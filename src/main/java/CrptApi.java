import com.google.gson.Gson;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class CrptApi {
    private static final String API_URL = "https://ismp.crpt.ru/api/v3/lk/documents/create";
    private final Semaphore semaphore;
    private final HttpClient httpClient;
    private final Gson gson;
    private Logger logger = Logger.getLogger(CrptApi.class.getName());

    public CrptApi() {
        this(0);
    }

    public CrptApi(int requestLimit) {
        this(new Semaphore(requestLimit), HttpClient.newBuilder().build(), createGson());
    }

    public CrptApi(Semaphore semaphore, HttpClient httpClient, Gson gson) {
        this.semaphore = semaphore;
        this.httpClient = httpClient;
        this.gson = gson;
    }

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this(requestLimit);
        long interval = timeUnit.toMillis(1);
        new Thread(new Cycle(interval, requestLimit)).start();
    }

    private static Gson createGson() {
        return new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .create();
    }

    public void createDocument(Documentable document, String signature) throws Exception {
        semaphore.acquire();
        String requestBody = gson.toJson(document);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(API_URL))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .header("Signature", signature)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to create document: " + response.body());
        }
        System.out.println("Response: " + response.body());
    }

    public interface Documentable {
    }

    public static class Document implements Documentable {
        private Descriptionable description;
        private DocumentInformationable documentInfo;
        private Boolean importRequest;
        private Innanble inns;
        private SuperProductionInfo productionInfo;
        private Product[] products;
        private Registrationable registrationInfo;

        public Document(Descriptionable description, DocumentInformationable documentInfo,
                        Boolean importRequest, Innanble inns, SuperProductionInfo productionInfo,
                        Product[] products, Registrationable registrationInfo) {
            this.description = description;
            this.documentInfo = documentInfo;
            this.importRequest = importRequest;
            this.inns = inns;
            this.productionInfo = productionInfo;
            this.products = products;
            this.registrationInfo = registrationInfo;
        }

        public Descriptionable getDescription() {
            return description;
        }

        public void setDescription(Descriptionable description) {
            this.description = description;
        }

        public DocumentInformationable getDocumentInfo() {
            return documentInfo;
        }

        public void setDocumentInfo(DocumentInformationable documentInfo) {
            this.documentInfo = documentInfo;
        }

        public Boolean getImportRequest() {
            return importRequest;
        }

        public void setImportRequest(Boolean importRequest) {
            this.importRequest = importRequest;
        }

        public Innanble getInns() {
            return inns;
        }

        public void setInns(Innanble inns) {
            this.inns = inns;
        }

        public SuperProductionInfo getProductionInfo() {
            return productionInfo;
        }

        public void setProductionInfo(SuperProductionInfo productionInfo) {
            this.productionInfo = productionInfo;
        }

        public Product[] getProducts() {
            return products;
        }

        public void setProducts(Product[] products) {
            this.products = products;
        }

        public Registrationable getRegistrationInfo() {
            return registrationInfo;
        }

        public void setRegistrationInfo(Registrationable registrationInfo) {
            this.registrationInfo = registrationInfo;
        }
    }

    public interface Descriptionable {
    }

    public static class Description implements Descriptionable {
        private String participiantInn;

        public Description(String participiantInn) {
            this.participiantInn = participiantInn;
        }

        public String getParticipiantInn() {
            return participiantInn;
        }

        public void setParticipiantInn(String participiantInn) {
            this.participiantInn = participiantInn;
        }
    }

    public interface DocumentInformationable {
    }

    public static class DocumentInformation implements DocumentInformationable {
        private String docId;
        private String docStatus;
        private String docType;

        public DocumentInformation(String docId, String docStatus, String docType) {
            this.docId = docId;
            this.docStatus = docStatus;
            this.docType = docType;
        }

        public String getDocId() {
            return docId;
        }

        public void setDocId(String docId) {
            this.docId = docId;
        }

        public String getDocStatus() {
            return docStatus;
        }

        public void setDocStatus(String docStatus) {
            this.docStatus = docStatus;
        }

        public String getDocType() {
            return docType;
        }

        public void setDocType(String docType) {
            this.docType = docType;
        }
    }

    public interface Innanble {
    }

    public interface SuperInnable extends Innanble {
    }

    public static class InnsImpl implements Innanble {
        private String ownerInn;
        private String partipiciantInn;

        public InnsImpl(String ownerInn, String partipiciantInn) {
            this.ownerInn = ownerInn;
            this.partipiciantInn = partipiciantInn;
        }

        public String getOwnerInn() {
            return ownerInn;
        }

        public void setOwnerInn(String ownerInn) {
            this.ownerInn = ownerInn;
        }

        public String getPartipiciantInn() {
            return partipiciantInn;
        }

        public void setPartipiciantInn(String partipiciantInn) {
            this.partipiciantInn = partipiciantInn;
        }
    }

    public static class Inns implements SuperInnable {
        private Innanble innanble;
        private String producerInn;

        public Inns(Innanble innanble, String producerInn) {
            this.innanble = innanble;
            this.producerInn = producerInn;
        }

        public Innanble getInnanble() {
            return innanble;
        }

        public void setInnanble(Innanble innanble) {
            this.innanble = innanble;
        }

        public String getProducerInn() {
            return producerInn;
        }

        public void setProducerInn(String producerInn) {
            this.producerInn = producerInn;
        }
    }

    public interface ProductionInfo {
    }

    public interface SuperProductionInfo extends ProductionInfo {
    }

    public static class ProductionInfoImpl implements ProductionInfo {
        private LocalDate date;

        public ProductionInfoImpl(LocalDate date) {
            this.date = date;
        }

        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }
    }

    public static class SuperProductionInfoImpl implements SuperProductionInfo {
        private ProductionInfo dateInfo;
        private String type;

        public SuperProductionInfoImpl(ProductionInfo dateInfo, String type) {
            this.dateInfo = dateInfo;
            this.type = type;
        }

        public ProductionInfo getDateInfo() {
            return dateInfo;
        }

        public void setDateInfo(ProductionInfo dateInfo) {
            this.dateInfo = dateInfo;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

    public interface Codeable {
    }

    public static class CodeableImpl implements Codeable {
        private String uitu;
        private String uit;
        private String tnved;

        public CodeableImpl(String uitu, String uit, String tnved) {
            this.uitu = uitu;
            this.uit = uit;
            this.tnved = tnved;
        }

        public String getUitu() {
            return uitu;
        }

        public void setUitu(String uitu) {
            this.uitu = uitu;
        }

        public String getUit() {
            return uit;
        }

        public void setUit(String uit) {
            this.uit = uit;
        }

        public String getTnved() {
            return tnved;
        }

        public void setTnved(String tnved) {
            this.tnved = tnved;
        }
    }

    public interface Certificatinable {
    }

    public static class CertificateDocument implements Certificatinable {
        private String certificateDocument;
        private LocalDate certificateDocumentDate;
        private String certificateDocumentNumber;

        public CertificateDocument(String certificateDocument, LocalDate certificateDocumentDate, String certificateDocumentNumber) {
            this.certificateDocument = certificateDocument;
            this.certificateDocumentDate = certificateDocumentDate;
            this.certificateDocumentNumber = certificateDocumentNumber;
        }

        public String getCertificateDocument() {
            return certificateDocument;
        }

        public void setCertificateDocument(String certificateDocument) {
            this.certificateDocument = certificateDocument;
        }

        public LocalDate getCertificateDocumentDate() {
            return certificateDocumentDate;
        }

        public void setCertificateDocumentDate(LocalDate certificateDocumentDate) {
            this.certificateDocumentDate = certificateDocumentDate;
        }

        public String getCertificateDocumentNumber() {
            return certificateDocumentNumber;
        }

        public void setCertificateDocumentNumber(String certificateDocumentNumber) {
            this.certificateDocumentNumber = certificateDocumentNumber;
        }
    }

    public interface Product {
    }

    public static class ProductImpl implements Product {
        private Certificatinable certificatinable;
        private Innanble innanble;
        private ProductionInfo productionInfo;
        private Codeable codeable;

        public ProductImpl(Certificatinable certificatinable, Innanble innanble, ProductionInfo productionInfo, Codeable codeable) {
            this.certificatinable = certificatinable;
            this.innanble = innanble;
            this.productionInfo = productionInfo;
            this.codeable = codeable;
        }

        public Certificatinable getCertificatinable() {
            return certificatinable;
        }

        public void setCertificatinable(Certificatinable certificatinable) {
            this.certificatinable = certificatinable;
        }

        public Innanble getInnanble() {
            return innanble;
        }

        public void setInnanble(Innanble innanble) {
            this.innanble = innanble;
        }

        public ProductionInfo getProductionInfo() {
            return productionInfo;
        }

        public void setProductionInfo(ProductionInfo productionInfo) {
            this.productionInfo = productionInfo;
        }

        public Codeable getCodeable() {
            return codeable;
        }

        public void setCodeable(Codeable codeable) {
            this.codeable = codeable;
        }
    }

    public interface Registrationable {
    }

    public static class RegistrationableImpl implements Registrationable {
        private LocalDate regDate;
        private String regNumber;

        public RegistrationableImpl(LocalDate regDate, String regNumber) {
            this.regDate = regDate;
            this.regNumber = regNumber;
        }

        public LocalDate getRegDate() {
            return regDate;
        }

        public void setRegDate(LocalDate regDate) {
            this.regDate = regDate;
        }

        public String getRegNumber() {
            return regNumber;
        }

        public void setRegNumber(String regNumber) {
            this.regNumber = regNumber;
        }
    }

    class Cycle implements Runnable {
        private volatile long interval;
        private int requestLimit;

        public Cycle(long interval, int requestLimit) {
            this.interval = interval;
            this.requestLimit = requestLimit;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    TimeUnit.MILLISECONDS.sleep(interval);
                    semaphore.release(requestLimit - semaphore.availablePermits());
                } catch (InterruptedException e) {
                    logger.info(e.getMessage());
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
    static class LocalDateAdapter extends TypeAdapter<LocalDate> {
        private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

        @Override
        public void write(JsonWriter jsonWriter, LocalDate localDate) throws IOException {
            jsonWriter.value(localDate.format(formatter));
        }

        @Override
        public LocalDate read(JsonReader jsonReader) throws IOException {
            return LocalDate.parse(jsonReader.nextString(), formatter);
        }
    }
    

}
class Main {
    public static void main(String[] args) {
        try {
            CrptApi api = new CrptApi(TimeUnit.SECONDS, 5);

            CrptApi.Description description = new CrptApi.Description("1234567890");
            CrptApi.DocumentInformation documentInfo = new CrptApi.DocumentInformation("1", "New", "Type1");
            CrptApi.InnsImpl innsImpl = new CrptApi.InnsImpl("1111111111", "2222222222");
            CrptApi.Inns inns = new CrptApi.Inns(innsImpl, "3333333333");
            CrptApi.ProductionInfoImpl productionInfoImpl = new CrptApi.ProductionInfoImpl(LocalDate.now());
            CrptApi.SuperProductionInfoImpl productionInfo = new CrptApi.SuperProductionInfoImpl(productionInfoImpl, "TypeA");
            CrptApi.CertificateDocument certificate = new CrptApi.CertificateDocument("Cert", LocalDate.now(), "12345");
            CrptApi.CodeableImpl code = new CrptApi.CodeableImpl("UITU", "UIT", "TNVED");
            CrptApi.ProductImpl product = new CrptApi.ProductImpl(certificate, innsImpl, productionInfoImpl, code);
            CrptApi.Product[] products = new CrptApi.Product[]{product};
            CrptApi.RegistrationableImpl registration = new CrptApi.RegistrationableImpl(LocalDate.now(), "Reg123");

            CrptApi.Document document = new CrptApi.Document(description, documentInfo, true, inns, productionInfo, products, registration);

            api.createDocument(document, "your-signature");
            System.out.println("Документ успешно создан");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}