package com.mediarium.softged.ingestion.service;

import com.mediarium.softged.ingestion.businessmodel.RenderedPage;
import com.mediarium.softged.shared.exception.TechnicalException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class PdfPageRenderingService {

    @Value("${app.storage.pages-path}")
    private String pagesStoragePath;

    public RenderedPage renderPage(
            Path pdfPath,
            Long documentId,
            String ownerUid,
            int pageNumber
    ) {
        try (PDDocument document = Loader.loadPDF(pdfPath.toFile())) {
            PDFRenderer renderer = new PDFRenderer(document);

            int pageIndex = pageNumber - 1;
            int dpi = 200;

            BufferedImage image = renderer.renderImageWithDPI(
                    pageIndex,
                    dpi,
                    ImageType.RGB
            );

            Path pageDirectory = Path.of(
                    pagesStoragePath,
                    ownerUid,
                    String.valueOf(documentId)
            );

            Files.createDirectories(pageDirectory);

            String filename = "page-" + pageNumber + ".png";
            Path targetPath = pageDirectory.resolve(filename);

            ImageIO.write(image, "png", targetPath.toFile());

            return new RenderedPage(
                    targetPath.toString(),
                    image.getWidth(),
                    image.getHeight()
            );

        } catch (Exception exception) {
            throw new TechnicalException("Unable to render PDF page " + pageNumber, exception);
        }
    }
}