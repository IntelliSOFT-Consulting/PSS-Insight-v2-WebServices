package com.intellisoft.internationalinstance.controller;

import com.intellisoft.internationalinstance.DbVersionData;
import com.intellisoft.internationalinstance.FormatterClass;
import com.intellisoft.internationalinstance.Results;
import com.intellisoft.internationalinstance.service_impl.service.InternationalService;
import com.intellisoft.internationalinstance.service_impl.service.NotificationService;
import com.intellisoft.internationalinstance.service_impl.service.VersionService;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping(value = "/api/v1/master-template")
@RestController
@RequiredArgsConstructor
public class MyController {
    private final VersionService versionService;
    private final NotificationService notificationService;
    FormatterClass formatterClass = new FormatterClass();
    private final InternationalService internationalService;

    @Operation(
            summary = "Pull the international indicators from the metadata json ",
            description = "This api is used for pulling the international indicators from the datastore")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "${api.response-codes.ok.desc}"),
            @ApiResponse(responseCode = "400", description = "${api.response-codes.badRequest.desc}",
                    content = { @Content(examples = { @ExampleObject(value = "") }) }),
            @ApiResponse(responseCode = "404", description = "${api.response-codes.notFound.desc}",
                    content = { @Content(examples = { @ExampleObject(value = "") }) }) })
    @GetMapping("/indicators")
    public ResponseEntity<?> getIndicatorForFrontEnd() {
        Results results = internationalService.getIndicators();
        return formatterClass.getResponse(results);
    }

    @Operation(
            summary = "Create a version using the provided indicators",
            description = "Post a version indicating if its a draft or a published version")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "${api.response-codes.ok.desc}"),
            @ApiResponse(responseCode = "400", description = "${api.response-codes.badRequest.desc}",
                    content = { @Content(examples = { @ExampleObject(value = "") }) }),
            @ApiResponse(responseCode = "404", description = "${api.response-codes.notFound.desc}",
                    content = { @Content(examples = { @ExampleObject(value = "") }) }) })
    @PostMapping("/version")
    public ResponseEntity<?> createVersion(
            @RequestBody DbVersionData dbVersionData) {

        Results results = internationalService.saveUpdate(dbVersionData);
        return formatterClass.getResponse(results);
    }

    @Operation(
            summary = "Update version details.",
            description = "You cannot update a published version")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "${api.response-codes.ok.desc}"),
            @ApiResponse(responseCode = "400", description = "${api.response-codes.badRequest.desc}",
                    content = { @Content(examples = { @ExampleObject(value = "") }) }),
            @ApiResponse(responseCode = "404", description = "${api.response-codes.notFound.desc}",
                    content = { @Content(examples = { @ExampleObject(value = "") }) }) })
    @PutMapping(value = "/version/{versionId}")
    public ResponseEntity<?> updateVersions(
            @RequestBody DbVersionData dbVersionData,
            @PathVariable("versionId") String versionId ){

        dbVersionData.setVersionId(Long.valueOf(versionId));

        Results results = internationalService.saveUpdate(dbVersionData);
        return formatterClass.getResponse(results);


    }

    @Operation(
            summary = "Version details ",
            description = "The api provides the version details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "${api.response-codes.ok.desc}"),
            @ApiResponse(responseCode = "400", description = "${api.response-codes.badRequest.desc}",
                    content = { @Content(examples = { @ExampleObject(value = "") }) }),
            @ApiResponse(responseCode = "404", description = "${api.response-codes.notFound.desc}",
                    content = { @Content(examples = { @ExampleObject(value = "") }) }) })
    @GetMapping(value = "/version/{versionId}")
    public ResponseEntity<?> getVersionDetails(@PathVariable("versionId") Long versionId){
        Results results = versionService.getVersion(versionId);
        return formatterClass.getResponse(results);

    }

    @Operation(
            summary = "Pull the saved versions.",
            description = "This api receives two statuses: DRAFT & PUBLISHED.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "${api.response-codes.ok.desc}"),
            @ApiResponse(responseCode = "400", description = "${api.response-codes.badRequest.desc}",
                    content = { @Content(examples = { @ExampleObject(value = "") }) }),
            @ApiResponse(responseCode = "404", description = "${api.response-codes.notFound.desc}",
                    content = { @Content(examples = { @ExampleObject(value = "") }) }) })
    @GetMapping(value = "/version")
    public ResponseEntity<?> getTemplates(
            @RequestParam(value = "limit", required = false) String limit,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "pageNo", required = false) String pageNo
    ){

        int limitNo = 10;
        if (limit != null && !limit.equals("")){
            limitNo = Integer.parseInt(limit);
        }
        String statusValue = "ALL";
        if (status != null && !status.equals("")){
            statusValue = status;
        }
        int pageNumber = 1;
        if (pageNo != null && !pageNo.equals("")){
            pageNumber = Integer.parseInt(pageNo);
        }

        Results results = versionService.getTemplates(pageNumber, limitNo, statusValue);
        return formatterClass.getResponse(results);

    }

    @Operation(
            summary = "Deletes a version ",
            description = "A published version cannot be deleted.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "${api.response-codes.ok.desc}"),
            @ApiResponse(responseCode = "400", description = "${api.response-codes.badRequest.desc}",
                    content = { @Content(examples = { @ExampleObject(value = "") }) }),
            @ApiResponse(responseCode = "404", description = "${api.response-codes.notFound.desc}",
                    content = { @Content(examples = { @ExampleObject(value = "") }) }) })
    @DeleteMapping(value = "/version/{versionId}")
    public ResponseEntity<?> deleteTemplate(@PathVariable("versionId") long versionId) {
        Results results = versionService.deleteTemplate(versionId);
        return formatterClass.getResponse(results);
    }

    @GetMapping("/create-pdf")
    public ResponseEntity<byte[]> createPdf() throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, outputStream);
        document.open();
        // add title to the document
        Font titleFont = new Font(Font.FontFamily.TIMES_ROMAN, 24, Font.BOLD);
        Paragraph title = new Paragraph("Pharmaceutical Products and Services", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        // add table to the document
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(20f);
        table.setSpacingAfter(20f);

        Font tableHeaderFont = new Font(Font.FontFamily.TIMES_ROMAN, 14, Font.BOLD);
        Font tableCellFont = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.NORMAL);

        PdfPCell cell;

        for (int i = 0 ; i < 3; i++){

            cell = new PdfPCell(new Phrase("Category Name", tableHeaderFont));
            cell.setBorderColor(BaseColor.BLACK);
            cell.setColspan(2);
            cell.setPaddingLeft(10);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            table.addCell(cell);

            for (int j = 0; j < 2; j++){
                cell = new PdfPCell(new Phrase("Indicator Name", tableHeaderFont));
                cell.setBorderColor(BaseColor.BLACK);
                cell.setPaddingLeft(10);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                table.addCell(cell);

                cell = new PdfPCell(new Phrase("Key "+i, tableCellFont));
                cell.setBorderColor(BaseColor.BLACK);
                cell.setPaddingLeft(10);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                table.addCell(cell);
            }



        }



        document.add(table);
        document.close();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("inline", "example.pdf");

        return new ResponseEntity<>(outputStream.toByteArray(), headers, HttpStatus.OK);
    }


}
