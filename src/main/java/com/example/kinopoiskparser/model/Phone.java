package com.example.kinopoiskparser.model;

import lombok.*;

/**
 * Модель телефона, содержащая основные характеристики, полученные с сайта.
 */
@Getter
@Setter
public class Phone {

    /**
     * Название модели телефона
     */
    private String title;

    /**
     * Цена телефона
     */
    private String price;

    /**
     * Кэшбэк, если доступен
     */
    private String cashback;

    /**
     * Количество отзывов
     */
    private String reviewCount;

    /**
     * Средний рейтинг
     */
    private String rating;

    /**
     * Характеристика экрана (диагональ, разрешение и т.д.)
     */
    private String screen;
}
