package com.web.filter.strategy;

public enum FilterClass {

    WITCH_CULT("witchculttranslation.com", WitchCultHtmlFilter.class),
    EMINENT("eminenttranslations.com", EminentHtmlFilter.class),
    CHICKEN("translationchicken.com", ChickenHtmlFilter.class);

    public final String domain;
    public final Class<? extends IHtmlFilter> filterClazz;

    FilterClass(String domain, Class<? extends IHtmlFilter> filterClazz) {
        this.domain = domain;
        this.filterClazz = filterClazz;
    }
}
