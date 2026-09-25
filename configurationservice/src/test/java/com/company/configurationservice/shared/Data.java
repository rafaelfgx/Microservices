package com.company.configurationservice.shared;

import com.company.configurationservice.configuration.Configuration;
import com.company.configurationservice.configuration.requests.CreateConfigurationRequest;
import com.company.configurationservice.configuration.requests.UpdateConfigurationRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Data {
    public static final String NON_EXISTENT_ID = "NonExistentId";
    public static final String ID = "Configuration";
    public static final String VALUE = "Value";
    public static final String UPDATED_VALUE = "UpdatedValue";
    public static final String DESCRIPTION = "Description";
    public static final String UPDATED_DESCRIPTION = "UpdatedDescription";
    public static final String GROUP = "Group";
    public static final String UPDATED_GROUP = "UpdatedGroup";

    public static final Configuration CONFIGURATION = new Configuration(ID, VALUE, DESCRIPTION, GROUP);
    public static final com.company.starter.configuration.Configuration CONFIGURATION_RESPONSE = new com.company.starter.configuration.Configuration(ID, VALUE, DESCRIPTION, GROUP);
    public static final CreateConfigurationRequest CREATE_CONFIGURATION_REQUEST = new CreateConfigurationRequest(ID, VALUE, DESCRIPTION, GROUP);
    public static final UpdateConfigurationRequest UPDATE_CONFIGURATION_REQUEST = new UpdateConfigurationRequest(ID, UPDATED_VALUE, UPDATED_DESCRIPTION, UPDATED_GROUP);
}
