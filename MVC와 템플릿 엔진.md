MVC: Model, View, Controller



### @RequestParam
- 브라우저에서 요청한 파라미터를 매핑할 수 있다.
- URL에 있는 name이라는 키(key)의 값을 찾아서, 바로 뒤에 선언된 변수 name에 할당해 준다.
- 브라우저 주소창에서 입력한 값은 HTTP 프로토콜을 타고 서버로 전달될 때 전부 텍스트로 전달된다.
- 따라서 만약 RequestParam으로 받을 변수를 Integer로 해 두면 스프링이 내부적으로 캐스팅을 한다.
```java
    @GetMapping("hello-mvc")
    public String helloMvc(@RequestParam("name") String name, Model model) {
        model.addAttribute("name", name);
        return "hello-template";
    }
```


### @ResponseBody로 문자 반환
- 보통 컨트롤러에서 String을 리턴하면 스프링을 해당 이름을 가지는 html일 리턴한다.
- 하지만 @ResponseBody가 붙으면 ViewResolver를 거치지 않고 리턴 값을 HTTP응답의 body 부분에 직접 담아 보내게 된다.
- 결과적으로 사용자는 HTML소스 코드가 아닌 순수한 문자열을 받게 된다.
```java
@GetMapping("hello-string")
@ResponseBody
public String helloString(@RequestParam("name") String name) {
    return "hello " + name;
} 
```


### @ResponseBody로 객체 반환
- 위와 달리 @ResponseBody가 붙은 상태에서 객체를 반환하게 되면 스프링이 객체를 JSON으로 변환해서 반환한다.
- 이걸 API 방식으로 볼 수 있다.
```java
@GetMapping("hello-api")
@ResponseBody
public Hello helloApi(@RequestParam("name") String name) {
    Hello hello = new Hello();
    hello.setName(name);
    return hello;
}
static class Hello {
    private String name;
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
}
```


### @ResponseBody 사용 원리
- HTTP의 BODY에 문자 내용을 직접 반환
- `viewResolver` 대신에 `HttpMessageConverter` 가 동작
- 기본 문자처리: `StringHttpMessageConverter`
- 기본 객체처리: `MappingJackson2HttpMessageConverter`
- byte 처리 등등 기타 여러 HttpMessageConverter가 기본으로 등록되어 있음

![img_2.png](img_2.png)