package ru.ticketservice.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.ticketservice.entity.Ticket;
import ru.ticketservice.exception.NotFoundException;
import ru.ticketservice.repository.TicketRepository;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

/**
 * @brief Сервис генерации PDF-билетов.
 *
 * Создаёт электронный билет в формате A5 (альбомная ориентация) с:
 * <ul>
 *   <li>шапкой в корпоративном стиле (золото + тёмно-фиолетовый)</li>
 *   <li>деталями мероприятия и места</li>
 *   <li>QR-кодом (изображение PNG, сгенерированное через ZXing)</li>
 * </ul>
 *
 * Для корректного отображения кириллицы используется шрифт Arial Unicode.
 */
@Service
@RequiredArgsConstructor
public class PdfService {

    private final TicketRepository ticketRepo;

    /** @brief Путь к файлу шрифта с поддержкой кириллицы (Arial Unicode). */
    private static final String FONT_PATH = "/Library/Fonts/Arial Unicode.ttf";

    /** @brief Золотой цвет для акцентов (#D4AF37). */
    private static final DeviceRgb GOLD  = new DeviceRgb(212, 175, 55);

    /** @brief Тёмно-фиолетовый цвет фона (#1A0A2E). */
    private static final DeviceRgb DARK  = new DeviceRgb(26, 10, 46);

    /** @brief Светло-лавандовый цвет ячеек (#F5F0FF). */
    private static final DeviceRgb LIGHT = new DeviceRgb(245, 240, 255);

    /**
     * @brief Генерирует PDF-файл билета для указанного пользователя.
     *
     * Проверяет принадлежность билета пользователю перед генерацией.
     *
     * @param ticketId идентификатор билета
     * @param userId   идентификатор запрашивающего пользователя
     * @return байтовый массив PDF-документа
     * @throws NotFoundException    если билет не найден или не принадлежит пользователю
     * @throws RuntimeException     при ошибке генерации PDF или QR-кода
     */
    public byte[] generateTicketPdf(Integer ticketId, Integer userId) {
        Ticket ticket = ticketRepo.findByIdAndUserId(ticketId, userId)
            .orElseThrow(() -> new NotFoundException("Билет не найден"));

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document doc = new Document(pdf, PageSize.A5.rotate());
            doc.setMargins(20, 20, 20, 20);

            PdfFont font = PdfFontFactory.createFont(FONT_PATH,
                    PdfEncodings.IDENTITY_H, PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
            doc.setFont(font);

            // ── Header ──────────────────────────────────────────────
            Table header = new Table(UnitValue.createPercentArray(new float[]{1}))
                    .useAllAvailableWidth();
            header.addCell(new Cell()
                    .add(new Paragraph("TicketService").setFontSize(22).setBold().setFontColor(GOLD))
                    .add(new Paragraph("Электронный билет").setFontSize(11).setFontColor(ColorConstants.WHITE))
                    .setBackgroundColor(DARK).setPadding(12).setBorder(null));
            doc.add(header);

            doc.add(new Paragraph(" "));

            // ── Event title ─────────────────────────────────────────
            doc.add(new Paragraph(ticket.getEvent().getTitle())
                    .setFontSize(18).setBold().setFontColor(DARK)
                    .setTextAlignment(TextAlignment.CENTER));

            doc.add(new Paragraph(" "));

            // ── Details + QR ────────────────────────────────────────
            Table body = new Table(UnitValue.createPercentArray(new float[]{3, 1}))
                    .useAllAvailableWidth();

            // Left: info cells
            Table info = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                    .useAllAvailableWidth();
            addInfoCell(info, "Дата",             ticket.getEvent().getDateTimeFormatted());
            addInfoCell(info, "Место проведения",
                    ticket.getEvent().getCity() + ", " + ticket.getEvent().getVenue());
            addInfoCell(info, "Место в зале",
                    "Ряд " + ticket.getSeat().getRowNumber() +
                    ", место " + ticket.getSeat().getSeatNumber() +
                    " (" + ticket.getSeat().getSector() + ")");
            addInfoCell(info, "Цена", ticket.getPrice().toPlainString() + " руб.");

            String issuedAt = ticket.getCreatedAt() != null
                    ? ticket.getCreatedAt().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
                    : "";
            addInfoCell(info, "Билет №", String.valueOf(ticket.getId()));
            addInfoCell(info, "Выдан", issuedAt);

            body.addCell(new Cell().add(info).setBorder(null).setPadding(0));

            // Right: QR code image
            byte[] qrBytes = generateQrPng(ticket.getQrCode(), 200);
            Image qrImage = new Image(ImageDataFactory.create(qrBytes))
                    .setWidth(120).setHeight(120)
                    .setHorizontalAlignment(HorizontalAlignment.CENTER);
            body.addCell(new Cell()
                    .add(qrImage)
                    .add(new Paragraph("Предъявите при входе")
                            .setFontSize(8).setItalic().setFontColor(DARK)
                            .setTextAlignment(TextAlignment.CENTER))
                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBorder(new SolidBorder(GOLD, 1))
                    .setBackgroundColor(LIGHT).setPadding(8));

            doc.add(body);
            doc.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка генерации PDF: " + e.getMessage(), e);
        }
    }

    /**
     * @brief Добавляет ячейку с меткой и значением в таблицу деталей билета.
     *
     * @param table таблица, в которую добавляется ячейка
     * @param label заголовок поля (серый мелкий текст)
     * @param value значение поля (тёмный жирный текст)
     */
    private void addInfoCell(Table table, String label, String value) {
        table.addCell(new Cell()
                .add(new Paragraph(label).setFontSize(9).setFontColor(ColorConstants.GRAY))
                .add(new Paragraph(value).setFontSize(11).setBold().setFontColor(DARK))
                .setBackgroundColor(LIGHT).setPadding(8)
                .setBorder(new SolidBorder(GOLD, 1)));
    }

    /**
     * @brief Генерирует QR-код как PNG-изображение.
     *
     * Использует библиотеку ZXing (com.google.zxing) для кодирования строки
     * в QR-код и преобразования BitMatrix в BufferedImage.
     *
     * @param content строка для кодирования (UUID билета)
     * @param size    размер изображения в пикселях (ширина = высота)
     * @return байтовый массив PNG-изображения
     * @throws Exception при ошибке кодирования или записи изображения
     */
    private byte[] generateQrPng(String content, int size) throws Exception {
        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size);
        BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", out);
        return out.toByteArray();
    }
}
