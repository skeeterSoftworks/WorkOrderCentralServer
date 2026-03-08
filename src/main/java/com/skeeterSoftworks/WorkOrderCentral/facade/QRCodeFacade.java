package com.skeeterSoftworks.WorkOrderCentral.facade;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import com.skeeterSoftworks.WorkOrderCentral.to.enums.EImageFormat;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.MockQrRequest;
import com.skeeterSoftworks.WorkOrderCentral.to.objects.QRMessage;
import com.skeeterSoftworks.WorkOrderCentral.util.ImageUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.awt.image.BufferedImage;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/qrcode")
public class QRCodeFacade {

	@Value("${mock.qr-sim.enabled:false}")
	private boolean mockQrSimEnabled;

	@Autowired
	SimpMessagingTemplate template;


	@PostMapping("/simulate")
	public ResponseEntity<?> simulateQrCodeReadout(@RequestBody MockQrRequest mockQrRequest) {

		log.debug("Facade call: simulateQrCodeReadout()");

		if (!StringUtils.hasText(mockQrRequest.getMockScannedQr())) {

			log.error("Mock qrContent is empty!");
			return ResponseEntity.badRequest().build();
		}

		if (!mockQrSimEnabled) {

			log.error("Mock qr sim is disabled!");

			return ResponseEntity.status(503).body("MOCK_QR_SIM_DISABLED");
		}

		try {
			QRMessage qrMessage = new QRMessage();
			qrMessage.setQrText(mockQrRequest.getMockScannedQr());
			qrMessage.setTimeStamp(LocalDateTime.now().toString());

			template.convertAndSend("/websocket/message", qrMessage);

			return ResponseEntity.ok().build();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return ResponseEntity.internalServerError().body("ERROR_SIMULATING_QR_READOUT");
		}
	}

	@GetMapping("/generate")
	public ResponseEntity<?> generateQR(@RequestParam String qrData) {

		qrData = qrData.replace("/r", "");

		try {
			QRCodeWriter barcodeWriter = new QRCodeWriter();
			BitMatrix bitMatrix = barcodeWriter.encode(qrData, BarcodeFormat.QR_CODE, 200, 200);

			BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

			return ResponseEntity.ok(ImageUtils.imageToBase64String(qrImage, EImageFormat.JPG));

		} catch (Exception e) {
			log.error(e.getMessage(), e);

			return ResponseEntity.internalServerError().body("ERROR_GENERATING_QR_CODE");
		}
	}
}
