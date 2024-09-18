package org.example;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.net.http.HttpClient;
import java.util.concurrent.*;

public class CrptApi {
    private final ObjectMapper objectMapper;
    private final Semaphore semaphore;
    private final ScheduledExecutorService scheduler;
    private final TimeUnit timeUnit;
    private final int requestLimit;

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        HttpClient httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        this.semaphore = new Semaphore(requestLimit);
        this.timeUnit = timeUnit;
        this.requestLimit = requestLimit;
        this.scheduler = Executors.newScheduledThreadPool(1);
        startResetTask();
    }

    private void startResetTask() {
        scheduler.scheduleAtFixedRate(() -> semaphore.release(requestLimit - semaphore.availablePermits()), 0, 1, timeUnit);
    }

    public void createDocument(Document document, String signature) throws IOException, InterruptedException {
        semaphore.acquire();
        try {
            String requestBody = createRequestBody(document, signature);
            String simulatedResponse = "{\"timestamp\":\"18-09-2024 09:57:52\",\"code\":200,\"message\":\"Success\"}";
            System.out.println("Response: " + simulatedResponse);
        } finally {
            semaphore.release();
        }
    }

    private String createRequestBody(Document document, String signature) throws IOException {
        ObjectNode rootNode = objectMapper.createObjectNode();
        rootNode.set("description", objectMapper.valueToTree(document));
        rootNode.put("signature", signature);
        return objectMapper.writeValueAsString(rootNode);
    }

    public void shutdown() {
        scheduler.shutdown();
    }

    public static class Document {
        public String participantInn;
        public String docId;
        public String docStatus;
        public String docType;
        public boolean importRequest;
        public String ownerInn;
        public String producerInn;
        public String productionDate;
        public String productionType;
        public Product[] products;
        public String regDate;
        public String regNumber;

        public static class Product {
            public String certificateDocument;
            public String certificateDocumentDate;
            public String certificateDocumentNumber;
            public String ownerInn;
            public String producerInn;
            public String productionDate;
            public String tnvedCode;
            public String uitCode;
            public String uituCode;
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        CrptApi crptApi = new CrptApi(TimeUnit.MINUTES, 10);

        Document document = new Document();
        document.participantInn = "1234567890";
        document.docId = "doc123";
        document.docStatus = "NEW";
        document.docType = "LP_INTRODUCE_GOODS";
        document.importRequest = true;
        document.ownerInn = "1234567890";
        document.producerInn = "0987654321";
        document.productionDate = "2023-09-18";
        document.productionType = "GOODS";
        document.regDate = "2023-09-18";
        document.regNumber = "reg123";

        Document.Product product = new Document.Product();
        product.certificateDocument = "cert123";
        product.certificateDocumentDate = "2023-09-18";
        product.certificateDocumentNumber = "certNum123";
        product.ownerInn = "1234567890";
        product.producerInn = "0987654321";
        product.productionDate = "2023-09-18";
        product.tnvedCode = "1234";
        product.uitCode = "uit123";
        product.uituCode = "uitu123";

        document.products = new Document.Product[]{product};

        crptApi.createDocument(document, "signature123");

        crptApi.shutdown();
    }
}