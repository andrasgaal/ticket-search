package com.asg.ticket.wizz.dto.search.response;

public class Fare {

    private final Price administrationFeePrice;
    private final Price basePrice;
    private final Price discountedPrice;
    private final Price fullBasePrice;
    private final Price discountedFarePrice;

    public Fare(Price administrationFeePrice, Price basePrice, Price discountedPrice, Price fullBasePrice, Price discountedFarePrice) {
        this.administrationFeePrice = administrationFeePrice;
        this.basePrice = basePrice;
        this.discountedPrice = discountedPrice;
        this.fullBasePrice = fullBasePrice;
        this.discountedFarePrice = discountedFarePrice;
    }

    public Price getAdministrationFeePrice() {
        return administrationFeePrice;
    }

    public Price getBasePrice() {
        return basePrice;
    }

    public Price getDiscountedPrice() {
        return discountedPrice;
    }

    public Price getFullBasePrice() {
        return fullBasePrice;
    }

    public Price getDiscountedFarePrice() {
        return discountedFarePrice;
    }
}
