package jpabook.jpashop.controller;

import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;
import java.util.List;

/**
 * 상품 컨트롤러
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {

    private final ItemService itemService;

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("form", new BookForm());
        return "items/createItemForm";
    }

    @PostMapping("/new")
    public String create(@ModelAttribute("form") @Valid BookForm form, BindingResult result) {

        if (result.hasErrors()) {
            return "items/createItemForm";
        }

        Book book = Book.createFromBookForm(form);

        itemService.saveItem(book);
        return "redirect:/items";
    }

    /**
     * 상품 목록
     */
    @GetMapping()
    public String list(Model model) {
        List<Item> items = itemService.findItems();
        model.addAttribute("items", items);
        return "items/itemList";
    }


    /**
     * 상품 수정 폼
     */
    @GetMapping("/{itemId}/edit")
    public String updateItemForm(@PathVariable("itemId") Long itemId, Model model) {
        //DB 조회
        Book item = (Book) itemService.findOne(itemId);
        //조회 결과를 DTO로 변환 후 모델에 추가
        BookForm form = BookForm. convertBookForm(item);
        model.addAttribute("form", form);

        return "items/updateItemForm";
    }


    /**
     * 상품 수정
     */
    @PostMapping("/{itemId}/edit")
    public String updateItem(@PathVariable String itemId, @ModelAttribute("form") @Valid BookForm form, BindingResult result) {

        if (result.hasErrors()) {
            return "items/updateItemForm";
        }

        Book book = Book.createFromBookForm(form);

        itemService.saveItem(book);
        return "redirect:/items";
    }
    /**
     * SQL Injection 주의점
     * @PathVariable로 {itemId}를 받는 형태에서, 값이 조작될 수 있는 보안상 취약점
     * 뒷단의 서비스 계층에서든 앞단에서든 현재 유저가 이 아이템에 대해서 권한 유무의 체크 로직이 서버에 있어야 됨
     * 또는 다소 복잡한 방법으로 업데이트 할 객체를 세션에 담아두고 풀어내는 방법도 있음(요즘엔 세션 객체를 잘 안씀)
     */
}
