package wooteco.subway.api;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import wooteco.subway.controller.path.PathController;
import wooteco.subway.domain.Line;
import wooteco.subway.domain.Station;
import wooteco.subway.dto.LineDetailResponse;
import wooteco.subway.dto.WholeSubwayResponse;
import wooteco.subway.service.PathService;

@WebMvcTest(controllers = {PathController.class})
public class PathControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PathService pathService;

    @Test
    void ETag() throws Exception {
        WholeSubwayResponse response = WholeSubwayResponse.of(
            Arrays.asList(createMockResponse(), createMockResponse()));
        given(pathService.wholeLines()).willReturn(response);

        String uri = "/paths/detail";

        MvcResult mvcResult = mockMvc.perform(get(uri))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(header().exists("ETag"))
            .andReturn();
        String eTag = mvcResult.getResponse().getHeader("ETag");

        mockMvc.perform(get(uri).header("If-None-Match", eTag))
            .andDo(print())
            .andExpect(status().isNotModified())
            .andExpect(header().exists("ETag"))
            .andReturn();
    }

    private LineDetailResponse createMockResponse() {
        List<Station> stations = Arrays.asList(new Station(), new Station(), new Station());
        return LineDetailResponse.of(new Line(), stations);
    }

    @DisplayName("예외테스트: 경로 조회 시 출발역 혹은 도착역에 빈 문자열 혹은 Null이 들어온 경우 예외 발생")
    @Test
    void searchPath_GivenBlankStation_ExceptionThrown() throws Exception {
        //given
        HashMap<String, String> params = new HashMap<>();
        params.put("source", "");
        params.put("target", "강남역");

        //when
        //then
        mockMvc.perform(get("/paths")
            .param("source", "")
            .param("target", "강남역")
            .param("type", "DISTANCE"))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andReturn();
    }
}
