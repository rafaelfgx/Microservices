package com.company.customerservice.customer;

import com.company.customerservice.customer.domains.Customer;
import com.company.customerservice.customer.requests.CreateCustomerRequest;
import com.company.customerservice.customer.responses.CustomerResponse;
import com.company.customerservice.shared.Data;
import com.company.customerservice.shared.SpringBootTestConfiguration;
import com.company.starter.clients.auth.AuthClient;
import com.company.starter.otp.GenerateOtpRequest;
import com.company.starter.otp.Otp;
import com.company.starter.otp.OtpService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import org.springframework.web.client.HttpClientErrorException;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@AutoConfigureMockMvc(addFilters = false)
@Import(SpringBootTestConfiguration.class)
@SpringBootTest
class CustomerIntegrationTest {
    @Autowired
    MockMvcTester mvc;

    @Autowired
    JsonMapper json;

    @Autowired
    MongoTemplate mongo;

    @Autowired
    AuthClient authClient;

    @Autowired
    OtpService otpService;

    @BeforeEach
    void beforeEach() {
        mongo.getDb().drop();
        Mockito.clearInvocations(authClient);
    }

    @ParameterizedTest
    @ValueSource(strings = {"?name=inexistent", "/" + Data.NIL_UUID})
    void shouldReturnNotFoundWhenCustomerDoesNotExist(final String uri) {
        mongo.save(Data.CUSTOMER);
        final var result = mvc.get().uri("/customers" + uri).exchange();
        Assertions.assertThat(result).hasStatus(HttpStatus.NOT_FOUND);
    }

    @ParameterizedTest
    @ValueSource(strings = {"page=0", "size=99999", "sort=id", "sort=name", "direction=ASC", "direction=DESC", "name=" + Data.NAME})
    void shouldReturnOkWhenCustomersExist(final String uri) {
        mongo.save(Data.CUSTOMER);
        final var result = mvc.get().uri("/customers?" + uri).exchange();
        Assertions.assertThat(result).hasStatus(HttpStatus.OK);
    }

    @Test
    void shouldReturnOkWhenCustomerExists() {
        mongo.save(Data.CUSTOMER);
        final var result = mvc.get().uri("/customers/{id}", Data.ID).exchange();
        Assertions.assertThat(result).hasStatus(HttpStatus.OK).bodyJson().convertTo(CustomerResponse.class).isEqualTo(Data.CUSTOMER_RESPONSE);
    }

    @Test
    void shouldReturnCreatedWhenCreatingCustomer() {
        final var result = mvc.post().uri("/customers").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(Data.CREATE_CUSTOMER_REQUEST)).exchange();
        final var id = Assertions.assertThat(result).hasStatus(HttpStatus.CREATED).bodyJson().convertTo(UUID.class).actual();
        Assertions.assertThat(mongo.findAll(Otp.class)).singleElement().satisfies(otp -> {
            Assertions.assertThat(otp.getType()).isEqualTo("CUSTOMER_VERIFICATION");
            Assertions.assertThat(otp.getKey()).isEqualTo(id.toString());
            Assertions.assertThat(otp.getHash()).isNotBlank();
        });
    }

    @Test
    void shouldReturnCreatedWhenOverwritingUnverifiedCustomer() {
        mongo.save(Data.CUSTOMER);
        otpService.generate(new GenerateOtpRequest("CUSTOMER_VERIFICATION", Data.ID));
        final var originalHash = mongo.findAll(Otp.class).getFirst().getHash();
        Mockito.when(authClient.save(ArgumentMatchers.any())).thenReturn(ResponseEntity.ok(Data.USER_ID)).thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(UUID.randomUUID()));
        final var request = new CreateCustomerRequest(Data.NAME_UPDATED, Data.EMAIL, Data.USERNAME, Data.PASSWORD);
        final var result = mvc.post().uri("/customers").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(request)).exchange();
        Assertions.assertThat(result).hasStatus(HttpStatus.CREATED).bodyJson().convertTo(UUID.class).isEqualTo(Data.CUSTOMER.getId());
        Assertions.assertThat(mongo.findAll(Customer.class)).singleElement().extracting(Customer::getName).isEqualTo(Data.NAME_UPDATED);
        Assertions.assertThat(mongo.findAll(Otp.class)).singleElement().satisfies(otp -> {
            Assertions.assertThat(otp.getKey()).isEqualTo(Data.ID);
            Assertions.assertThat(otp.getHash()).isNotEqualTo(originalHash);
        });
    }

    @Test
    void shouldReturnConflictWhenCustomerAlreadyExists() {
        mongo.save(Data.CUSTOMER);
        final var conflict = new HttpClientErrorException(HttpStatus.CONFLICT, "Conflict", null, null, null);
        conflict.setBodyConvertFunction(_ -> ProblemDetail.forStatus(HttpStatus.CONFLICT));
        Mockito.when(authClient.save(ArgumentMatchers.any())).thenThrow(conflict).thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(UUID.randomUUID()));
        final var result = mvc.post().uri("/customers").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(Data.CREATE_CUSTOMER_REQUEST)).exchange();
        Assertions.assertThat(result).hasStatus(HttpStatus.CONFLICT);
        Assertions.assertThat(mongo.findAll(Customer.class)).containsExactly(Data.CUSTOMER);
    }

    @ParameterizedTest
    @ValueSource(strings = {Data.NIL_UUID, Data.ID})
    void shouldReturnNoContentWhenUpdatingCustomer(final String id) {
        mongo.save(Data.CUSTOMER);
        final var result = mvc.put().uri("/customers/{id}", id).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(Data.UPDATE_CUSTOMER_REQUEST)).exchange();
        Assertions.assertThat(result).hasStatus(HttpStatus.NO_CONTENT);
    }

    @Test
    void shouldVerifyTheUserAssociatedWithTheCustomer() {
        mongo.save(Data.CUSTOMER);
        final var otp = otpService.generate(new GenerateOtpRequest("CUSTOMER_VERIFICATION", Data.ID));
        final var result = verify(Data.ID, otp);
        Assertions.assertThat(result).hasStatus(HttpStatus.NO_CONTENT);
        Mockito.verify(authClient).verify(Data.USER_ID);
        Assertions.assertThat(verify(Data.ID, otp)).hasStatus(HttpStatus.BAD_REQUEST);
        Mockito.verify(authClient, Mockito.times(1)).verify(Data.USER_ID);
    }

    @Test
    void shouldRejectVerificationOfUnknownCustomer() {
        final var otp = otpService.generate(new GenerateOtpRequest("CUSTOMER_VERIFICATION", Data.NIL_UUID));
        final var result = verify(Data.NIL_UUID, otp);
        Assertions.assertThat(result).hasStatus(HttpStatus.NOT_FOUND);
        Mockito.verify(authClient, Mockito.never()).verify(ArgumentMatchers.any());
    }

    @Test
    void shouldRejectInvalidOtpBeforeLookingUpUnknownCustomer() {
        final var result = verify(Data.NIL_UUID, "123456");
        Assertions.assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
        Mockito.verify(authClient, Mockito.never()).verify(ArgumentMatchers.any());
    }

    @Test
    void shouldReturnBadRequestWhenVerifyingInvalidCustomerId() {
        final var result = verify("invalid", "123456");
        Assertions.assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
        Mockito.verify(authClient, Mockito.never()).verify(ArgumentMatchers.any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " "})
    void shouldRejectBlankVerificationOtp(final String otp) {
        mongo.save(Data.CUSTOMER);
        Assertions.assertThat(verify(Data.ID, otp)).hasStatus(HttpStatus.BAD_REQUEST);
        Mockito.verify(authClient, Mockito.never()).verify(ArgumentMatchers.any());
    }

    @Test
    void shouldRejectIncorrectOtpAndLimitAttempts() {
        mongo.save(Data.CUSTOMER);
        final var otp = otpService.generate(new GenerateOtpRequest("CUSTOMER_VERIFICATION", Data.ID));

        for (var attempt = 0; attempt < 3; attempt++) {
            Assertions.assertThat(verify(Data.ID, "incorrect")).hasStatus(HttpStatus.BAD_REQUEST);
        }

        Assertions.assertThat(verify(Data.ID, otp)).hasStatus(HttpStatus.BAD_REQUEST);
        Mockito.verify(authClient, Mockito.never()).verify(ArgumentMatchers.any());
    }

    @Test
    void shouldRejectOtpForAnotherCustomer() {
        mongo.save(Data.CUSTOMER);
        final var otp = otpService.generate(new GenerateOtpRequest("CUSTOMER_VERIFICATION", Data.NIL_UUID));
        Assertions.assertThat(verify(Data.ID, otp)).hasStatus(HttpStatus.BAD_REQUEST);
        Mockito.verify(authClient, Mockito.never()).verify(ArgumentMatchers.any());
    }

    @Test
    void shouldRejectMissingOtp() {
        final var result = mvc.patch().uri("/customers/{id}/verify", Data.ID).contentType(MediaType.APPLICATION_JSON).content("{}").exchange();
        Assertions.assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
        Mockito.verify(authClient, Mockito.never()).verify(ArgumentMatchers.any());
    }

    @Test
    void shouldRejectExpiredOtp() {
        mongo.save(Data.CUSTOMER);
        final var otp = otpService.generate(new GenerateOtpRequest("CUSTOMER_VERIFICATION", Data.ID));
        mongo.updateFirst(Query.query(Criteria.where("key").is(Data.ID)), new Update().set("timestamp", Instant.now().minus(Duration.ofHours(1))), Otp.class);
        Assertions.assertThat(verify(Data.ID, otp)).hasStatus(HttpStatus.BAD_REQUEST);
        Mockito.verify(authClient, Mockito.never()).verify(ArgumentMatchers.any());
    }

    @Test
    void shouldPropagateAuthServiceFailureWhenVerifyingCustomer() {
        mongo.save(Data.CUSTOMER);
        final var otp = otpService.generate(new GenerateOtpRequest("CUSTOMER_VERIFICATION", Data.ID));
        final var forbidden = new HttpClientErrorException(HttpStatus.FORBIDDEN, "Forbidden", null, null, null);
        forbidden.setBodyConvertFunction(_ -> ProblemDetail.forStatus(HttpStatus.FORBIDDEN));
        Mockito.when(authClient.verify(Data.USER_ID)).thenThrow(forbidden).thenReturn(ResponseEntity.noContent().build());
        final var result = verify(Data.ID, otp);
        Assertions.assertThat(result).hasStatus(HttpStatus.FORBIDDEN);
        Mockito.verify(authClient).verify(Data.USER_ID);
        Assertions.assertThat(verify(Data.ID, otp)).hasStatus(HttpStatus.BAD_REQUEST);
    }

    @ParameterizedTest
    @ValueSource(strings = {Data.NIL_UUID, Data.ID})
    void shouldReturnNoContentWhenDeletingCustomer(final String id) {
        mongo.save(Data.CUSTOMER);
        final var result = mvc.delete().uri("/customers/" + id).exchange();
        Assertions.assertThat(result).hasStatus(HttpStatus.NO_CONTENT);
    }

    private MvcTestResult verify(final String id, final String otp) {
        return mvc.patch().uri("/customers/{id}/verify", id).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(Map.of("otp", otp))).exchange();
    }
}
