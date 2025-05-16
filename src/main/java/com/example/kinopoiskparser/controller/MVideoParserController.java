package com.example.kinopoiskparser.controller;

import com.example.kinopoiskparser.model.Phone;
import com.example.kinopoiskparser.service.MVideoParserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Контроллер для получения списка смартфонов с сайта M.Video.
 */
@RestController
@RequestMapping("/api/phones")
public class MVideoParserController {

    private final MVideoParserService parserService;


    public MVideoParserController(MVideoParserService parserService) {
        this.parserService = parserService;
    }

    /**
     * Получение всех смартфонов, найденных при парсинге.
     *
     * @return список объектов Phone
     */
    @GetMapping
    public List<Phone> getAllPhones() {
        return parserService.parseAllPhones();
    }
}
