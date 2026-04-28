package com.studyagent.service;

import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@Service
@Slf4j
public class OcrService {

    public String recognizeText(MultipartFile image) {
        try {
            BufferedImage bufferedImage = ImageIO.read(image.getInputStream());
            if (bufferedImage == null) {
                throw new RuntimeException("无法读取图片格式");
            }

            ITesseract instance = new Tesseract();

            // 查找 tessdata 路径
            String tessdataPath = findTessdataPath();
            log.info("Using tessdata path: {}", tessdataPath);
            instance.setDatapath(tessdataPath);

            // 只用英文测试
            instance.setLanguage("eng");

            String result = instance.doOCR(bufferedImage);
            log.info("OCR识别完成，文字长度: {}", result.length());
            return result;

        } catch (IOException | TesseractException e) {
            log.error("OCR处理失败", e);
            throw new RuntimeException("OCR处理失败: " + e.getMessage());
        }
    }

    private String findTessdataPath() {
        // 检查环境变量
        String envPath = System.getenv("TESSDATA_PREFIX");
        if (envPath != null && new File(envPath).exists()) {
            return envPath;
        }

        // 检查常见路径
        String[] paths = {
            "C:\\Program Files\\Tesseract-OCR\\tessdata",
            "C:\\tessdata",
            "C:\\Users\\" + System.getProperty("user.name") + "\\tessdata",
            "tessdata",
            "./tessdata"
        };

        for (String path : paths) {
            File f = new File(path);
            if (f.exists() && new File(path + "\\eng.traineddata").exists()) {
                return path;
            }
        }

        // 返回第一个存在的路径
        for (String path : paths) {
            if (new File(path).exists()) {
                return path;
            }
        }

        return ".";
    }
}