/**
 * NOTE: This class is auto generated by the swagger code generator program (3.0.4-SNAPSHOT).
 * https://github.com/swagger-api/swagger-codegen
 * Do not edit the class manually.
 */
package de.adorsys.psd2.api;

import de.adorsys.psd2.model.ConfirmationOfFunds;
import de.adorsys.psd2.model.Error400NGAIS;
import de.adorsys.psd2.model.Error401NGPIIS;
import de.adorsys.psd2.model.Error403NGPIIS;
import de.adorsys.psd2.model.Error404NGPIIS;
import de.adorsys.psd2.model.Error405NGPIIS;
import de.adorsys.psd2.model.Error409NGPIIS;
import de.adorsys.psd2.model.InlineResponse2003;
import java.util.UUID;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Optional;
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2020-08-31T12:29:06.642536+03:00[Europe/Kiev]")

@Api(value = "ConfirmationOfFundsServicePiis", description = "the ConfirmationOfFundsServicePiis API")
public interface FundsConfirmationApi {

    Logger log = LoggerFactory.getLogger(FundsConfirmationApi.class);

    default Optional<ObjectMapper> getObjectMapper() {
        return Optional.empty();
    }

    default Optional<HttpServletRequest> getRequest() {
        return Optional.empty();
    }

    default Optional<String> getAcceptHeader() {
        return getRequest().map(r -> r.getHeader("Accept"));
    }

    @ApiOperation(value = "Confirmation of funds request", nickname = "checkAvailabilityOfFunds", notes = "Creates a confirmation of funds request at the ASPSP. Checks whether a specific amount is available at point of time of the request on an account linked to a given tuple card issuer(TPP)/card number, or addressed by IBAN and TPP respectively. If the related extended services are used a conditional Consent-ID is contained in the header. This field is contained but commented out in this specification.", response = InlineResponse2003.class, tags={  })
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = InlineResponse2003.class),
        @ApiResponse(code = 400, message = "Bad Request", response = Error400NGAIS.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = Error401NGPIIS.class),
        @ApiResponse(code = 403, message = "Forbidden", response = Error403NGPIIS.class),
        @ApiResponse(code = 404, message = "Not found", response = Error404NGPIIS.class),
        @ApiResponse(code = 405, message = "Method Not Allowed", response = Error405NGPIIS.class),
        @ApiResponse(code = 406, message = "Not Acceptable"),
        @ApiResponse(code = 408, message = "Request Timeout"),
        @ApiResponse(code = 409, message = "Conflict", response = Error409NGPIIS.class),
        @ApiResponse(code = 415, message = "Unsupported Media Type"),
        @ApiResponse(code = 429, message = "Too Many Requests"),
        @ApiResponse(code = 500, message = "Internal Server Error"),
        @ApiResponse(code = 503, message = "Service Unavailable") })
    @RequestMapping(value = "/v1/funds-confirmations",
        produces = { "application/json", "application/problem+json" },
        consumes = { "application/json" },
        method = RequestMethod.POST)
    default ResponseEntity<InlineResponse2003> _checkAvailabilityOfFunds(@ApiParam(value = "Request body for a confirmation of funds request. " ,required=true )  @Valid @RequestBody ConfirmationOfFunds body,@ApiParam(value = "ID of the request, unique to the call, as determined by the initiating party." ,required=true) @RequestHeader(value="X-Request-ID", required=true) UUID xRequestID,@ApiParam(value = "This field  might be used in case where a consent was agreed between ASPSP and PSU through an OAuth2 based protocol,  facilitated by the TPP. " ) @RequestHeader(value="Authorization", required=false) String authorization,@ApiParam(value = "ID of the corresponding consent object as returned by confirmation of funds consent request. " ) @RequestHeader(value="Consent-ID", required=false) String consentID,@ApiParam(value = "Is contained if and only if the \"Signature\" element is contained in the header of the request." ) @RequestHeader(value="Digest", required=false) String digest,@ApiParam(value = "A signature of the request by the TPP on application level. This might be mandated by ASPSP. " ) @RequestHeader(value="Signature", required=false) String signature,@ApiParam(value = "The certificate used for signing the request, in base64 encoding.  Must be contained if a signature is contained. " ) @RequestHeader(value="TPP-Signature-Certificate", required=false) byte[] tpPSignatureCertificate) {
        return checkAvailabilityOfFunds(body, xRequestID, authorization, consentID, digest, signature, tpPSignatureCertificate);
    }

    // Override this method
    default ResponseEntity<InlineResponse2003> checkAvailabilityOfFunds(ConfirmationOfFunds body,UUID xRequestID,String authorization,String consentID,String digest,String signature,byte[] tpPSignatureCertificate) {
        if(getObjectMapper().isPresent() && getAcceptHeader().isPresent()) {
        } else {
            log.warn("ObjectMapper or HttpServletRequest not configured in default ConfirmationOfFundsServicePiisApi interface so no example is generated");
        }
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

}
