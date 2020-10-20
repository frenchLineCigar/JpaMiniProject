package jpabook.jpashop.controller;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

/**
 * 회원 컨트롤러
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;

    @GetMapping(value = "/new")
    public String createForm(Model model) {
        model.addAttribute("memberForm", new MemberForm()); //컨트롤러에서 뷰로 넘어갈때 이 모델 데이터를 싣어서 넘김
        return "members/createMemberForm";
    }

    @PostMapping(value = "/new")
    public String create(@Valid MemberForm form, BindingResult result) {

        if (result.hasErrors()) {
            return "members/createMemberForm";
        }

        Address address = new Address(form.getCity(), form.getStreet(), form.getZipcode());

        Member member = new Member();
        member.setName(form.getName());
        member.setAddress(address);

        memberService.join(member);
        return "redirect:/"; //홈으로 리다이렉트
    }

    @GetMapping()
    public String list(Model model) {
//        List<Member> members = memberService.findMembers();
//        model.addAttribute("members", members);

        List<Member> members = memberService.findMembers();
        List<MemberDto> mList = DtoUtils.convertDto(members);

        model.addAttribute("mList", mList);
        return "members/memberList";
    }

    static class DtoUtils {
        //convert MemberEntity into MemberDto
        private static List<MemberDto> convertDto(List<Member> members) {
            List<MemberDto> mList = new ArrayList<>();
            members.forEach(member -> {
                MemberDto m = new MemberDto();
                m.setId(member.getId());
                m.setCity(member.getAddress().getCity());
                m.setStreet(member.getAddress().getStreet());
                m.setZipcode(member.getAddress().getZipcode());
                mList.add(m);
            });
            return mList;
        }

    }

}

