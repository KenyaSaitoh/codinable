package pro.kensait.jakartaee.greet.rest;

/** JSON 本文 {"name":"Alice"} を受け取る型 */
public record GreetingRequest(String name) {
}
