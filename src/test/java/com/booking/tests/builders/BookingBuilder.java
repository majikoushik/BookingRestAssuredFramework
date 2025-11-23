package com.booking.tests.builders;

import com.booking.tests.models.BookingModels;
import java.time.LocalDate;

public final class BookingBuilder {
    private final BookingModels.Booking b = new BookingModels.Booking();

    public BookingBuilder() {
        b.firstname = "John";
        b.lastname = "Doe";
        b.totalprice = 150;
        b.depositpaid = true;
        b.bookingdates = new BookingModels.BookingDates(LocalDate.of(2025, 12, 20), LocalDate.of(2025, 12, 22));
        b.additionalneeds = "Breakfast";
    }
    public BookingBuilder name(String f, String l){ b.firstname=f; b.lastname=l; return this; }
    public BookingBuilder price(int p){ b.totalprice=p; return this; }
    public BookingBuilder dates(LocalDate in, LocalDate out){ b.bookingdates = new BookingModels.BookingDates(in,out); return this; }
    public BookingBuilder needs(String n){ b.additionalneeds=n; return this; }
    public BookingModels.Booking build(){ return b; }
}
