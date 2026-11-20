#!/usr/bin/env bash

set -euo pipefail

cd "$(dirname "$(readlink -f "${BASH_SOURCE[0]}")")"

modules=(parent starter)

services=(
    'authservice 8010'
    'configurationservice 8015'
)

write_section() {
    local title=$1
    local width=${COLUMNS:-$(tput cols 2>/dev/null || echo 80)}
    local separator
    printf -v separator '%*s' "$((width - 1))" ''
    separator=${separator// /=}
    printf '%s\n %s \n%s\n' "$separator" "${title^^}" "$separator"
}

invoke_step() {
    local title=$1
    shift
    write_section "$title"
    "$@" || exit 1
}

stop_ports() {
    local entry port pid
    for entry in "${services[@]}"; do
        read -r _ port <<<"$entry"
        while read -r pid; do
            kill -9 "$pid" 2>/dev/null || true
        done < <(ss -lntpH "sport = :$port" 2>/dev/null | grep -o 'pid=[0-9]\+' | cut -d= -f2 | sort -u)
    done
}

wait_port() {
    until [[ -n $(ss -lntH "sport = :$1") ]]; do sleep 1; done
}

start_docker() {
    invoke_step 'Docker' docker compose -f .docker/docker-compose.yaml up --detach --remove-orphans
}

build_modules() {
    local module
    for module in "${modules[@]}"; do
        invoke_step "$module" mvn -f "$module" clean install -DskipTests
    done
}

start_service() {
    mvn -f "$1" spring-boot:run -DskipTests &
    wait_port "$2"
}

start_services() {
    local entry name port
    for entry in "${services[@]}"; do
        read -r name port <<<"$entry"
        invoke_step "$name" start_service "$name" "$port"
    done
}

stop_ports
start_docker
build_modules
start_services
write_section 'Ready'
read -r || true
