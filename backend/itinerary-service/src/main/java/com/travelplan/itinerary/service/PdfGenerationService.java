package com.travelplan.itinerary.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import com.travelplan.itinerary.model.Itinerary;
import com.travelplan.itinerary.model.ItineraryDay;
import com.travelplan.itinerary.model.ItineraryActivity;
import com.travelplan.itinerary.repository.ItineraryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class PdfGenerationService {
    private final ItineraryRepository itineraryRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public byte[] generateItineraryPDF(Long itineraryId, String touristId) {
        log.info("Generating PDF for itinerary {}", itineraryId);

        Itinerary itinerary = itineraryRepository.findByIdAndTouristId(itineraryId, touristId)
                .orElseThrow(() -> new IllegalArgumentException("Itinerary not found or access denied"));

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter.getInstance(document, baos);
            
            document.open();

            // Header
            addHeader(document, itinerary);

            // Trip Overview
            addTripOverview(document, itinerary);

            // Day-by-day schedule
            addDayByDaySchedule(document, itinerary);

            // Budget Summary
            addBudgetSummary(document, itinerary);

            document.close();

            log.info("PDF generated successfully for itinerary {}", itineraryId);
            return baos.toByteArray();

        } catch (DocumentException e) {
            log.error("Error generating PDF for itinerary {}", itineraryId, e);
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }

    private void addHeader(Document document, Itinerary itinerary) throws DocumentException {
        Paragraph title = new Paragraph(itinerary.getTitle(), new Font(Font.HELVETICA, 24, Font.BOLD));
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        Paragraph dates = new Paragraph(
                String.format("%s - %s",
                        itinerary.getStartDate().format(DATE_FORMATTER),
                        itinerary.getEndDate().format(DATE_FORMATTER)),
                new Font(Font.HELVETICA, 12)
        );
        dates.setAlignment(Element.ALIGN_CENTER);
        document.add(dates);

        if (itinerary.getDescription() != null && !itinerary.getDescription().isEmpty()) {
            Paragraph description = new Paragraph(itinerary.getDescription(), new Font(Font.HELVETICA, 10));
            description.setAlignment(Element.ALIGN_CENTER);
            document.add(description);
        }

        document.add(Chunk.NEWLINE);
    }

    private void addTripOverview(Document document, Itinerary itinerary) throws DocumentException {
        Paragraph sectionTitle = new Paragraph("Trip Overview", new Font(Font.HELVETICA, 14, Font.BOLD));
        document.add(sectionTitle);

        Table table = new Table(2);
        table.setWidth(100);
        table.setPadding(5);

        Cell cell1 = new Cell(new Paragraph("Status", new Font(Font.HELVETICA, 10, Font.BOLD)));
        Cell cell2 = new Cell(new Paragraph(itinerary.getStatus().toString(), new Font(Font.HELVETICA, 10)));
        table.addCell(cell1);
        table.addCell(cell2);

        long days = java.time.temporal.ChronoUnit.DAYS.between(itinerary.getStartDate(), itinerary.getEndDate()) + 1;
        Cell cell3 = new Cell(new Paragraph("Duration", new Font(Font.HELVETICA, 10, Font.BOLD)));
        Cell cell4 = new Cell(new Paragraph(days + " days", new Font(Font.HELVETICA, 10)));
        table.addCell(cell3);
        table.addCell(cell4);

        document.add(table);
        document.add(Chunk.NEWLINE);
    }

    private void addDayByDaySchedule(Document document, Itinerary itinerary) throws DocumentException {
        Paragraph sectionTitle = new Paragraph("Day-by-Day Schedule", new Font(Font.HELVETICA, 14, Font.BOLD));
        document.add(sectionTitle);

        for (ItineraryDay day : itinerary.getDays()) {
            Paragraph dayTitle = new Paragraph(
                    String.format("Day %d - %s", day.getDayNumber(), day.getDate().format(DATE_FORMATTER)),
                    new Font(Font.HELVETICA, 12, Font.BOLD)
            );
            document.add(dayTitle);

            if (day.getActivities().isEmpty()) {
                Paragraph noActivities = new Paragraph("No activities planned");
                document.add(noActivities);
            } else {
                for (ItineraryActivity activity : day.getActivities()) {
                    addActivityDetails(document, activity);
                }
            }

            if (day.getNotes() != null && !day.getNotes().isEmpty()) {
                Paragraph notes = new Paragraph("Notes: " + day.getNotes(), new Font(Font.HELVETICA, 9, Font.ITALIC));
                document.add(notes);
            }

            document.add(Chunk.NEWLINE);
        }
    }

    private void addActivityDetails(Document document, ItineraryActivity activity) throws DocumentException {
        StringBuilder sb = new StringBuilder();
        sb.append("• ").append(activity.getTitle());

        if (activity.getStartTime() != null) {
            sb.append(" (").append(activity.getStartTime().format(TIME_FORMATTER));
            if (activity.getEndTime() != null) {
                sb.append(" - ").append(activity.getEndTime().format(TIME_FORMATTER));
            }
            sb.append(")");
        }

        if (activity.getLocation() != null) {
            sb.append(" at ").append(activity.getLocation());
        }

        Paragraph activityParagraph = new Paragraph(sb.toString(), new Font(Font.HELVETICA, 10));
        activityParagraph.setIndentationLeft(20);
        document.add(activityParagraph);

        if (activity.getDescription() != null && !activity.getDescription().isEmpty()) {
            Paragraph description = new Paragraph(activity.getDescription(), new Font(Font.HELVETICA, 9, Font.ITALIC));
            description.setIndentationLeft(30);
            document.add(description);
        }
    }

    private void addBudgetSummary(Document document, Itinerary itinerary) throws DocumentException {
        if (itinerary.getTotalBudget() != null) {
            document.add(Chunk.NEWLINE);
            Paragraph sectionTitle = new Paragraph("Budget Summary", new Font(Font.HELVETICA, 14, Font.BOLD));
            document.add(sectionTitle);

            Table table = new Table(2);
            table.setWidth(100);
            table.setPadding(5);

            Cell cell1 = new Cell(new Paragraph("Total Budget", new Font(Font.HELVETICA, 10, Font.BOLD)));
            Cell cell2 = new Cell(new Paragraph("$" + itinerary.getTotalBudget(), new Font(Font.HELVETICA, 10)));
            table.addCell(cell1);
            table.addCell(cell2);

            Cell cell3 = new Cell(new Paragraph("Amount Spent", new Font(Font.HELVETICA, 10, Font.BOLD)));
            Cell cell4 = new Cell(new Paragraph("$" + (itinerary.getActualSpent() != null ? itinerary.getActualSpent() : "0"), new Font(Font.HELVETICA, 10)));
            table.addCell(cell3);
            table.addCell(cell4);

            document.add(table);
        }
    }
}
