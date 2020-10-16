package jpabook.jpashop.service;

import jpabook.jpashop.domain.Category;
import jpabook.jpashop.domain.item.Album;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.domain.item.Movie;
import jpabook.jpashop.repository.ItemRepository;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import java.util.List;

import static org.junit.Assert.*;

/**
 * 상품 기능 테스트
 */

@RunWith(SpringRunner.class) //JUnit4 실행 시 스프링과 같이 실행
@SpringBootTest //스프링 부트를 띄운 상태로 테스트(스프링 컨테이너를 띄워 테스트 케이스에서 @Autowired 사용)
@Transactional
public class ItemServiceTest {

    @Autowired ItemService itemService;
    @Autowired ItemRepository itemRepository;
    @Autowired EntityManager em;
    
    @Test
    public void 상품등록() throws Exception {
        //given
        Album album = new Album();
        album.setArtist("artistA");
        album.setEtc("etcA");
        album.setName("albumA_byArtistA");
        album.setPrice(10000);
        album.addStock(100);
        album.removeStock(10);
        //when
        Long savedId = itemService.saveItem(album);

        //then
        assertEquals(album, itemRepository.findOne(savedId));
        assertSame(album, itemRepository.findOne(savedId));
    }

    @Test
    public void 아이디로_상품조회() throws Exception {
        //given
        Movie movie = new Movie();
        movie.setActor("actorA");
        movie.setDirector("directorA");
        movie.setName("movieA");
        movie.setPrice(700000);
        movie.addStock(50);

        //when
        Long savedId = itemService.saveItem(movie);

        //then
        assertEquals(movie, itemRepository.findOne(savedId));
    }

    @Test
    public void 이름으로_상품조회() throws Exception {
        //given
        Book book1 = new Book();
        book1.setAuthor("authorA");
        book1.setIsbn("isbnA");
        book1.setName("피노키오");
        book1.setPrice(20000);
        book1.addStock(10);
        
        Book book2 = new Book();
        book2.setAuthor("authorA");
        book2.setIsbn("isbnA");
        book2.setName("헨젤과_그레텔");
        book2.setPrice(20000);
        book2.addStock(10);

        Book book3 = new Book();
        book3.setAuthor("authorA");
        book3.setIsbn("isbnA");
        book3.setName("헨젤과_그레텔");
        book3.setPrice(20000);
        book3.addStock(10);

        //when
        itemService.saveItem(book1);
        itemService.saveItem(book2);
        itemService.saveItem(book3);

        //then
        Assertions.assertThat(itemRepository.findByName("헨젤과_그레텔").size()).isEqualTo(2);
        Assertions.assertThat(itemRepository.findByName("피노키오").size()).isEqualTo(1);
    }

    @Test
    public void 상품조회_전체목록() throws Exception {
        //given
        Album album = new Album();
        album.setArtist("artistA");
        album.setEtc("etcA");
        album.setName("albumA_byArtistA");
        album.setPrice(10000);
        album.addStock(100);
        album.removeStock(10);

        Book book = new Book();
        book.setAuthor("authorA");
        book.setIsbn("isbnA");
        book.setName("bookA_byAuthorA");
        book.setPrice(20000);
        book.addStock(10);

        Movie movie = new Movie();
        movie.setActor("actorA");
        movie.setDirector("directorA");
        movie.setName("movieA");
        movie.setPrice(700000);
        movie.addStock(50);

        //when
        Long savedAlbumItemId = itemService.saveItem(album);
        Long savedBookItemId = itemService.saveItem(book);
        Long savedMovieItemId = itemService.saveItem(movie);
//        List<Item> items = itemService.findItems();

        //then
        assertEquals(3,itemRepository.findAll().size());
//        assertEquals(items.size(),itemRepository.findAll().size());
        assertEquals(savedAlbumItemId,itemRepository.findAll().get(0).getId());
        assertEquals(savedBookItemId,itemRepository.findAll().get(1).getId());
        assertEquals(savedMovieItemId,itemRepository.findAll().get(2).getId());
        assertEquals(90, itemRepository.findAll().get(0).getStockQuantity());
        assertEquals(10000, itemRepository.findAll().get(0).getPrice());
        assertEquals(20000, itemRepository.findAll().get(1).getPrice());
        assertEquals(700000, itemRepository.findAll().get(2).getPrice());
    }


}