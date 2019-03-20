package org.smartregister.reveal.util;

public interface Constants {

    String VIEW_CONFIGURATION_PREFIX = "ViewConfiguration_";

    String FILTER_TEAM_ID = "teamId";

    String JSON_FORM_PARAM_JSON = "json";

    int REQUEST_CODE_GET_JSON = 3432;

    String METADATA = "metadata";

    String DETAILS = "details";

    String ENTITY_ID = "entity_id";

    String SPRAY_EVENT = "Spray";

    String REGISTER_STRUCTURE_EVENT = "Register_Structure";

    String STRUCTURE = "Structure";

    double MY_LOCATION_ZOOM_LEVEL = 17.5; // modifying this will also necessitate modifying the VERTICAL_OFFSET

    double VERTICAL_OFFSET = -0.0003;

    int ANIMATE_TO_LOCATION_DURATION = 1000;

    interface CONFIGURATION {
        String LOGIN = "login";
        String GLOBAL_CONFIGS = "global_configs";
        String KEY = "key";
        String VALUE = "value";
        String LOCATION_BUFFER_RADIUS_IN_METRES = "location_buffer_radius_in_metres";
        Float DEFAULT_LOCATION_BUFFER_RADIUS_IN_METRES = 25f;
        String UPDATE_LOCATION_BUFFER_RADIUS = "update_location_buffer_radius";
    }

    interface Preferences {
        String CURRENT_OPERATIONAL_AREA = "CURRENT_OPERATIONAL_AREA";
        String CURRENT_FACILITY = "CURRENT_FACILITY";
        String CURRENT_DISTRICT = "CURRENT_DISTRICT";
        String CURRENT_CAMPAIGN = "CURRENT_CAMPAIGN";
        String CURRENT_CAMPAIGN_ID = "CURRENT_CAMPAIGN_ID";
    }

    interface Tags {
        String COUNTRY = "Country";
        String PROVINCE = "Province";
        String DISTRICT = "District";
        String HEALTH_CENTER = "Rural Health Centre";
        String OPERATIONAL_AREA = "Operational Area";
    }

    interface Properties {
        String TASK_IDENTIFIER = "taskIdentifier";
        String TASK_BUSINESS_STATUS = "taskBusinessStatus";
        String TASK_STATUS = "taskStatus";
        String TASK_CODE = "taskCode";
        String LOCATION_UUID = "locationUUID";
        String LOCATION_VERSION = "locationVersion";
        String LOCATION_TYPE = "locationType";
    }


    interface GeoJSON {
        String TYPE = "type";
        String FEATURE_COLLECTION = "FeatureCollection";
        String FEATURES = "features";
    }

    interface Intervention {
        String IRS = "IRS";
        String ITN = "ITN";
        String MDA = "MDA";

        String MOSQUITO_COLLECTION = "Mosquito Collection";

        String LARVAL_DIPPING = "Larval Dipping";

        String IRS_VISIT = "IRS Visit";
        String BCC = "BCC";
    }

    interface BusinessStatus {
        String NOT_VISITED = "Not Visited";
        String NOT_SPRAYED = "Not Sprayed";
        String SPRAYED = "Sprayed";
        String NOT_SPRAYABLE = "Not Sprayable";
    }


    interface Map {
        int MAX_SELECT_ZOOM_LEVEL = 16;
        int CLICK_SELECT_RADIUS = 24;
    }

    interface JsonForm {

        String ENCOUNTER_TYPE = "encounter_type";

        String SPRAY_STATUS = "sprayStatus";

        String STRUCTURE_TYPE = "structureType";

        String HEAD_OF_HOUSEHOLD = "familyHeadName";

        String STRUCTURE_PROPERTIES_TYPE = "[structure_type]";

        String SPRAY_FORM = "json.form/spray_form.json";

        String ADD_STRUCTURE_FORM = "json.form/add_structure.json";

        String OPERATIONAL_AREA_TAG = "operational_area";

        String STRUCTURES_TAG = "structures";

        String NO_PADDING = "no_padding";

        String IS_MANDATORY = "is_mandatory";

        String SHORTENED_HINT = "shortened_hint";

        String HINT = "hint";
    }

    interface DateFormat {

        String EVENT_DATE_FORMAT_Z = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

        String EVENT_DATE_FORMAT_XXX = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

        String CARD_VIEW_DATE_FORMAT = "dd MMM yyyy";
    }

    interface Action {
        String STRUCTURE_TASK_SYNCHED = "reveal.STRUCTURE_TASK_SYNCHED";
    }


    interface StructureType {
        String RESIDENTIAL = "Residential Structure";

        String NON_RESIDENTIAL = "Non-Residential Structure";

        String MOSQUITO_COLLECTION_POINT = "Mosquito Collection Point";

        String LARVAL_BREEDING_SITE = "Larval Breeding Site";
    }

    interface TaskRegister {
        String VIEW_IDENTIFIER = "task_register";

        String INTERVENTION_TYPE = "intervention_type";

    }

    interface DatabaseKeys {

        String TASK_TABLE = "task";

        String SPRAYED_STRUCTURES = "sprayed_structures";

        String STRUCTURES_TABLE = "structure";

        String ID = "_id";

        String CODE = "code";

        String FOR = "for";

        String BUSINESS_STATUS = "business_status";

        String FAMILY_NAME = "family_head_name";

        String LATITUDE = "latitude";
        String LONGITUDE = "longitude";
        String NAME = "name";
    }


}
