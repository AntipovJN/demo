package rest.message.saving.app.demo.entity;

import lombok.*;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Date;

@Entity
@Getter @Setter
@EqualsAndHashCode
@AllArgsConstructor
public class Notification {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "message_id")
    private Message message;
    private Timestamp time;
    private Boolean delivered;

    public Notification() {
        this.time = new Timestamp(new Date().getTime());
        this.delivered = false;
    }

}
