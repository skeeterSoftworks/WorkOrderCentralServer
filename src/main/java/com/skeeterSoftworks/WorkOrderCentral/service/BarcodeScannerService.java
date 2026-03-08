package com.skeeterSoftworks.WorkOrderCentral.service;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

import com.skeeterSoftworks.WorkOrderCentral.to.objects.QRMessage;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
public class BarcodeScannerService {

	@Value("${barcodeScanner.port:COM3}")
	private String scannerPort;

	@Value("${barcodeScanner.readoutDelay.millis:1000}")
	private int readoutDelayMillis;

	@Autowired
	SimpMessagingTemplate template;


	@PostConstruct
	private void init() {

		SerialPort comPort = SerialPort.getCommPort(scannerPort);
		comPort.openPort();

		comPort.addDataListener(new SerialPortDataListener() {
			@Override
			public int getListeningEvents() {
				return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
			}

			@Override
			public void serialEvent(SerialPortEvent event) {
				if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE)
					return;

				try {
					Thread.sleep(readoutDelayMillis);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
				}

				byte[] newData = new byte[comPort.bytesAvailable()];

				int numRead = comPort.readBytes(newData, newData.length);
				log.info("Read " + numRead + " bytes.");

				String qrString = new String(newData);

				qrString = qrString.replace("\r", "");
				qrString = qrString.replace("\n", "");

				log.info(qrString);

				QRMessage qrMessage = new QRMessage();
				qrMessage.setQrText(qrString);
				qrMessage.setTimeStamp(LocalDateTime.now().toString());

				template.convertAndSend("/websocket/message", qrMessage);

			}
		});
		log.info("Barcode port open: {}", scannerPort);
	}

}
