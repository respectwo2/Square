package org.shax3.square.domain.s3.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.shax3.square.domain.s3.dto.request.PresignedPutUrlRequest;
import org.shax3.square.domain.s3.dto.response.PresignedPutUrlResponse;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.MalformedURLException;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class S3ServiceTest {

    @Mock
    private S3Presigner s3Presigner;

    @InjectMocks
    private S3Service s3Service;

    @BeforeEach
    void setUp() {
        // S3Service가 @Value("${cloud.aws.s3.bucket}")로 주입받는 값을 테스트 시점에 강제로 주입
        ReflectionTestUtils.setField(s3Service, "bucketName", "test-bucket");
    }

    @Test
    void testGeneratePresignedPutUrl() throws MalformedURLException {
        // given
        PresignedPutObjectRequest mockPutObjectRequest = Mockito.mock(PresignedPutObjectRequest.class);

        when(mockPutObjectRequest.url()).thenReturn(
                URI.create("https://test-bucket.s3.amazonaws.com/fake-uuid.png").toURL()
        );

        when(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class)))
                .thenReturn(mockPutObjectRequest);

        //when
        PresignedPutUrlRequest requestDto = new PresignedPutUrlRequest("example.png", "image/png", "profile");
        PresignedPutUrlResponse response = s3Service.generatePresignedPutUrl(requestDto);

        // then
        assertNotNull(response);
        assertEquals("https://test-bucket.s3.amazonaws.com/fake-uuid.png", response.presignedPutUrl());
    }
}
