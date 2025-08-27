package com.back.ourlog.global.client.spotify;

import com.back.ourlog.external.spotify.client.SpotifyClient;
import com.back.ourlog.external.spotify.dto.SpotifySearchResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SpotifyClientTest {

    @Autowired
    private SpotifyClient spotifyClient;

    @Test
    void testSearchTrack() {
        String keyword = "IU";

        SpotifySearchResponse response = spotifyClient.searchTrack(keyword);

        assertNotNull(response);
        assertFalse(response.getTracks().getItems().isEmpty());
    }
}
