package com.zjw.tools;

import cn.hutool.core.io.FileUtil;
import com.aliyun.oss.OSS;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.zjw.config.oss.OssConfig;
import com.zjw.constant.FileConstant;
import jakarta.annotation.Resource;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * PDF 生成工具
 *
 * @author ZhangJw
 * @date 2026年05月08日 9:03
 */
public class PDFGenerationTool {

    @Tool(description = "Generate a PDF file with given content", returnDirect = false)
    public String generatePDFLocalLocal(
            @ToolParam(description = "Name of the file to save the generated PDF") String fileName,
            @ToolParam(description = "Content to be included in the PDF") String content) {
        String fileDir = FileConstant.FILE_SAVE_DIR + "/pdf";
        String filePath = fileDir + "/" + fileName;
        try {
            // 创建目录
            FileUtil.mkdir(fileDir);
            // 创建 PdfWriter 和 PdfDocument 对象
            try (PdfWriter writer = new PdfWriter(filePath);
                 PdfDocument pdf = new PdfDocument(writer);
                 Document document = new Document(pdf)) {
                // 自定义字体（需要人工下载字体文件到特定目录）
//                String fontPath = Paths.get("src/main/resources/static/fonts/simsun.ttf")
//                        .toAbsolutePath().toString();
//                PdfFont font = PdfFontFactory.createFont(fontPath,
//                        PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
                // 使用内置中文字体
                PdfFont font = PdfFontFactory.createFont("STSongStd-Light", "UniGB-UCS2-H");
                document.setFont(font);
                // 创建段落
                Paragraph paragraph = new Paragraph(content);
                // 添加段落并关闭文档
                document.add(paragraph);
            }
            return "PDF generated successfully to: " + filePath;
        } catch (IOException e) {
            return "Error generating PDF: " + e.getMessage();
        }
    }

    @Resource
    private OSS ossClient;

    @Resource
    private OssConfig ossConfig;

    @Tool(description = "Generate a PDF file with given content and upload to object storage", returnDirect = true)
    public String generatePDFOss(
            @ToolParam(description = "Name of the file to save the generated PDF") String fileName,
            @ToolParam(description = "Content to be included in the PDF") String content) {
        try {
            // 使用 ByteArrayOutputStream 将 PDF 写入内存
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            // 创建 PdfWriter 和 PdfDocument 对象
            try (PdfWriter writer = new PdfWriter(outputStream);
                 PdfDocument pdf = new PdfDocument(writer);
                 Document document = new Document(pdf)) {

                // 使用内置中文字体
                PdfFont font = PdfFontFactory.createFont("STSongStd-Light", "UniGB-UCS2-H");
                document.setFont(font);

                // 创建段落
                Paragraph paragraph = new Paragraph(content);

                // 添加段落并关闭文档
                document.add(paragraph);
            }

            // 生成唯一的对象存储路径
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String uniqueFileName = UUID.randomUUID().toString().replace("-", "") + "_" + fileName;
            String ossKey = ossConfig.getPdfPathPrefix() + "/" + timestamp + "/" + uniqueFileName;

            // 上传到OSS
            ossClient.putObject(
                    ossConfig.getBucketName(),
                    ossKey,
                    new ByteArrayInputStream(outputStream.toByteArray())
            );

            // 构建访问URL
            String fileUrl = "https://" + ossConfig.getBucketName() + "."
                    + ossConfig.getEndpoint() + "/" + ossKey;

            return "PDF generated and uploaded successfully. URL: " + fileUrl;

        } catch (IOException e) {
            return "Error generating PDF: " + e.getMessage();
        } catch (Exception e) {
            return "Error uploading PDF to object storage: " + e.getMessage();
        }
    }
}