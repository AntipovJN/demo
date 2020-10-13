package rest.message.saving.app.demo.entity;

import lombok.*;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Getter @Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    private String messageContent;
    private Timestamp time;

    public Message(String messageContent) {
        this.messageContent = messageContent;
    }

    @Override
    public String toString() {
        return String.format("{\"messageContent\":\"%s\"}", messageContent);
    }
}
