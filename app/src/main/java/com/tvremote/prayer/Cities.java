package com.tvremote.prayer;

import java.time.ZoneId;

/** Turkish cities (approximate city-centre coordinates). */
public class Cities {
    public static final ZoneId ZONE = ZoneId.of("Europe/Istanbul");

    public static final String[] NAMES = {
        "Istanbul", "Ankara", "Izmir", "Bursa", "Antalya", "Adana", "Konya", "Gaziantep",
        "Sanliurfa", "Kayseri", "Mersin", "Diyarbakir", "Samsun", "Trabzon", "Eskisehir",
        "Denizli", "Erzurum", "Van", "Malatya", "Kahramanmaras", "Hatay", "Sakarya",
        "Kocaeli", "Tekirdag", "Edirne", "Canakkale", "Balikesir", "Manisa", "Aydin",
        "Mugla", "Sivas", "Elazig", "Batman", "Mardin", "Rize", "Ordu", "Kutahya", "Afyon"
    };
    public static final double[] LAT = {
        41.0082, 39.9334, 38.4237, 40.1885, 36.8969, 37.0000, 37.8746, 37.0662,
        37.1591, 38.7312, 36.8121, 37.9144, 41.2867, 41.0027, 39.7767,
        37.7765, 39.9043, 38.4891, 38.3552, 37.5858, 36.2012, 40.6940,
        40.7654, 40.9781, 41.6771, 40.1553, 39.6484, 38.6191, 37.8560,
        37.2153, 39.7477, 38.6810, 37.8812, 37.3212, 41.0201, 40.9839, 39.4167, 38.7507
    };
    public static final double[] LNG = {
        28.9784, 32.8597, 27.1428, 29.0610, 30.7133, 35.3213, 32.4932, 37.3833,
        38.7969, 35.4787, 34.6415, 40.2306, 36.3300, 39.7168, 30.5206,
        29.0864, 41.2679, 43.4089, 38.3095, 36.9371, 36.1602, 30.4358,
        29.9408, 27.5112, 26.5557, 26.4142, 27.8826, 27.4289, 27.8416,
        28.3636, 37.0179, 39.2264, 41.1351, 40.7245, 40.5234, 37.8764, 29.9833, 30.5567
    };
}
