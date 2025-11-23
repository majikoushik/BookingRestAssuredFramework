package com.booking.tests.models;

import java.time.LocalDate;

public class BookingModels {
    public static class BookingDates {
        public LocalDate checkin;
        public LocalDate checkout;
        public BookingDates() {}
        public BookingDates(LocalDate in, LocalDate out) { this.checkin = in; this.checkout = out; }
    }
    public static class Booking {
        public String firstname;
        public String lastname;
        public int totalprice;
        public boolean depositpaid;
        public BookingDates bookingdates;
        public String additionalneeds;
    }
    public static class CreateBookingResponse {
        public int bookingid;
        public Booking booking;
    }
    public static class AuthResponse {
        public String token;
    }
}
