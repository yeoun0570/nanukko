package nanukko.nanukko_back.dto.page;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
//페이징 처리를 프론트를 전달하기 위한 DTO
public class PageResponseDTO<T> {
    private List<T> content;        //실제 데이터
    private int totalPages;         //전체 페이지수
    private Long totalElements;     //전체 데이터수
    private int currentPage;        //현재 페이지
    private int size;               //페이지 크기
    private boolean first;          //첫 페이지 여부
    private boolean last;           //마지막 페이지 여부
    private boolean hasNext;        //다음 페이지 존재 여부
    private boolean hasPrevious;    //이전 페이지 존재 여부

    //Page 객체로부터 PageResponseDTO를 생성하는 생성자
    public PageResponseDTO(Page<T> page) {
        this.content = page.getContent();
        this.totalPages = page.getTotalPages();
        this.totalElements = page.getTotalElements();
        this.currentPage = page.getNumber();
        this.size = page.getSize();
        this.first = page.isFirst();
        this.last = page.isLast();
        this.hasNext = page.hasNext();
        this.hasPrevious = page.hasPrevious();
    }

    public static <T> PageResponseDTO<T> empty(Pageable pageable) { //검색어가 빈 경우 빈 결과 반환
        Page<T> emptyPage = Page.empty(pageable);
        return new PageResponseDTO<>(emptyPage);
    }
}
