package com.example.democaptchapuzzle;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.Random;

@RestController
@RequestMapping("/captcha")
public class Controller {

    @Value("${slide.image}")
    private String slideImages;

    @Value("${slide.template}")
    private String slideTemplate;

    @GetMapping("/get")
    public void get() throws Exception {
        File imgCatalog = new File(slideImages);
        File tempImgFile = new File(slideTemplate);

        long a = tempImgFile.length();
        File[] files = imgCatalog.listFiles();

        int randNum = new Random().nextInt(files.length);
        File targetFile = files[1];

        Slide slide = PuzzleUtils.pictureTemplatesCut(tempImgFile, targetFile);
        System.out.println(slide.getBigImage());
        System.out.println(slide.getSmallImage());
        System.out.println(slide.getXWidth());
        System.out.println(slide.getYHeight());
    }
}
