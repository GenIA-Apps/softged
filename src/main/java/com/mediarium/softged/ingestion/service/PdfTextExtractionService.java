package com.mediarium.softged.ingestion.service;

import com.mediarium.softged.shared.exception.TechnicalException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

@Service
public class PdfTextExtractionService {

    public int getPageCount(Path pdfPath) {
        try (PDDocument document = Loader.loadPDF(pdfPath.toFile())) {
            return document.getNumberOfPages();
        } catch (Exception exception) {
            throw new TechnicalException("Unable to read PDF page count", exception);
        }
    }

    public String extractTextFromPage(Path pdfPath, int pageNumber) {
        try (PDDocument document = Loader.loadPDF(pdfPath.toFile())) {
            PDFTextStripper stripper = new PDFTextStripper();

            stripper.setStartPage(pageNumber);
            stripper.setEndPage(pageNumber);

            return stripper.getText(document);
        } catch (Exception exception) {
            throw new TechnicalException("Unable to extract text from PDF page " + pageNumber, exception);
        }
    }
}