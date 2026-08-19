package com.support.backend.config;

import com.support.backend.entity.Admin;
import com.support.backend.entity.Category;
import com.support.backend.entity.Ticket;
import com.support.backend.entity.TicketMessage;
import com.support.backend.enums.AuthorType;
import com.support.backend.enums.MessageDirection;
import com.support.backend.enums.TicketStatus;
import com.support.backend.repository.AdminRepository;
import com.support.backend.repository.CategoryRepository;
import com.support.backend.repository.TicketMessageRepository;
import com.support.backend.repository.TicketRepository;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Seeds reference categories and a set of sample tickets so the admin UI has
 * realistic data to work against while real email intake does not yet exist.
 * Enabled by {@code app.seed.demo-data} (true in dev; set false for any non-dev run).
 * Runs after {@link AdminSeeder} so seeded tickets can be assigned to the default admin.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "app.seed.demo-data", havingValue = "true")
public class DemoDataSeeder {

    private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);

    /** One message in a seeded conversation thread. */
    private record Msg(MessageDirection direction, AuthorType authorType, boolean fromAdmin, String body) {

        static Msg customer(String body) {
            return new Msg(MessageDirection.INBOUND, AuthorType.CUSTOMER, false, body);
        }

        static Msg admin(String body) {
            return new Msg(MessageDirection.OUTBOUND, AuthorType.ADMIN, true, body);
        }

        static Msg ai(String body) {
            return new Msg(MessageDirection.OUTBOUND, AuthorType.AI, false, body);
        }
    }

    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE)
    public CommandLineRunner seedDemoData(
            CategoryRepository categoryRepository,
            TicketRepository ticketRepository,
            TicketMessageRepository messageRepository,
            AdminRepository adminRepository,
            JdbcTemplate jdbcTemplate) {
        return args -> {
            Map<String, Category> cat = seedCategories(categoryRepository);
            if (ticketRepository.count() > 0) {
                return;
            }
            Admin admin = adminRepository.findAll().stream().findFirst().orElse(null);
            var seeder = new TicketSeeder(ticketRepository, messageRepository, jdbcTemplate, admin, new Random());

            seeder.create("Cannot log in to my account", "sarah.jones@example.com", "Sarah Jones",
                    TicketStatus.ESCALATED, cat.get("Account"), false,
                    Msg.customer("Hi, I've been locked out of my account since this morning. Resetting my password didn't help. Please assist."),
                    Msg.customer("Still can't get in — this is urgent, I have a client demo in an hour."));

            seeder.create("Double charged on my last invoice", "mike.roberts@example.com", "Mike Roberts",
                    TicketStatus.OPEN, cat.get("Billing"), true,
                    Msg.customer("I was charged twice for the November plan. Can you refund the duplicate charge?"),
                    Msg.admin("Thanks for reaching out, Mike. I can see the duplicate charge and I've started the refund — it should land in 3-5 business days."),
                    Msg.customer("Great, thank you! Will I get an email confirmation?"),
                    Msg.admin("Yes, a receipt for the refund will be emailed to you once it clears."));

            seeder.create("How do I export my data?", "lena.k@example.com", "Lena K",
                    TicketStatus.AUTO_RESOLVED, cat.get("Technical"), false,
                    Msg.customer("Is there a way to export all my records to CSV?"),
                    Msg.ai("You can export to CSV from Settings > Data > Export. Click 'Export all' and you'll get a download link by email."));

            seeder.create("Feature request: dark mode", "devfan@example.com", "Alex P",
                    TicketStatus.NEW, cat.get("Feedback"), false,
                    Msg.customer("Love the product! Any chance of a dark mode for the dashboard?"));

            seeder.create("App is very slow today", "gwen@example.com", "Gwen S",
                    TicketStatus.PENDING_CUSTOMER, cat.get("Technical"), true,
                    Msg.customer("The dashboard takes 30+ seconds to load since this morning."),
                    Msg.admin("Sorry about that. Could you tell us which browser and roughly what time you noticed the slowdown?"));

            seeder.create("Thanks for the quick help!", "priya.m@example.com", "Priya M",
                    TicketStatus.RESOLVED, cat.get("General"), true,
                    Msg.customer("Just wanted to say your support team sorted my issue really fast. Thank you!"),
                    Msg.admin("So glad we could help, Priya. Reach out any time!"));

            seeder.create("WIN A FREE IPHONE NOW!!!", "promo@spammy-domain.example", null,
                    TicketStatus.SPAM, null, false,
                    Msg.customer("Congratulations!!! Click here to claim your prize!!!"));

            seeder.create("Invoice PDF won't download", "tom.hardy@example.com", "Tom Hardy",
                    TicketStatus.OPEN, cat.get("Billing"), true,
                    Msg.customer("When I click download invoice, nothing happens in Safari."),
                    Msg.admin("Thanks Tom — this is a known Safari pop-up blocker issue. Try holding Option while clicking, or switch to Chrome for the download."),
                    Msg.customer("Option-click worked. Thanks!"));

            seeder.create("Need to change my billing email", "finance@acme.example", "Acme Finance",
                    TicketStatus.OPEN, cat.get("Account"), false,
                    Msg.customer("Please update our billing contact to finance@acme.example."));

            seeder.create("Password reset email never arrives", "jkline@example.com", "J. Kline",
                    TicketStatus.ESCALATED, cat.get("Account"), true,
                    Msg.customer("I've requested a reset five times and nothing shows up, even in spam."),
                    Msg.admin("Let me check our mail logs. Can you confirm the exact address you're using?"),
                    Msg.customer("It's jkline@example.com — the same one I'm emailing from."));

            seeder.create("API rate limit questions", "dev@startup.example", "Startup Dev",
                    TicketStatus.PENDING_CUSTOMER, cat.get("Technical"), true,
                    Msg.customer("What's the rate limit on the reporting endpoint? We're getting 429s."),
                    Msg.admin("The reporting endpoint allows 60 requests/min. Are you batching or calling per-record?"));

            seeder.create("Upgrade to the annual plan", "owner@shop.example", "Shop Owner",
                    TicketStatus.RESOLVED, cat.get("Billing"), true,
                    Msg.customer("I'd like to switch from monthly to annual to get the discount."),
                    Msg.admin("Done! You're on the annual plan now and the prorated credit was applied."),
                    Msg.customer("Perfect, appreciate it."));

            seeder.create("Great onboarding experience", "newuser@example.com", "Nina W",
                    TicketStatus.CLOSED, cat.get("Feedback"), false,
                    Msg.customer("Just finished setup — the guided tour was really helpful. No issues!"));

            seeder.create("Report totals look wrong", "analyst@corp.example", "Data Analyst",
                    TicketStatus.OPEN, cat.get("Technical"), true,
                    Msg.customer("The monthly revenue total is double what I expect for June."),
                    Msg.admin("Thanks — can you share the report ID so I can reproduce it on our side?"),
                    Msg.customer("Report ID is RPT-2291."),
                    Msg.admin("Got it, I can reproduce. Looks like refunds are being counted as positive. Escalating to engineering."));

            seeder.create("Can I add a second admin?", "founder@team.example", "Founder",
                    TicketStatus.AUTO_RESOLVED, cat.get("Account"), false,
                    Msg.customer("How do I invite a teammate as an admin?"),
                    Msg.ai("Go to Settings > Team > Invite, enter their email and pick the Admin role. They'll get an invite link by email."));

            seeder.create("Mobile layout is broken", "mobile@example.com", "Sam T",
                    TicketStatus.NEW, cat.get("Technical"), false,
                    Msg.customer("On my phone the ticket list overflows off the screen."));

            seeder.create("Refund request for unused month", "unhappy@example.com", "Chris D",
                    TicketStatus.ESCALATED, cat.get("Billing"), false,
                    Msg.customer("I forgot to cancel and got billed. Can I get a refund for this month?"));

            seeder.create("Love the new dashboard", "fan2@example.com", "Riley B",
                    TicketStatus.CLOSED, cat.get("Feedback"), false,
                    Msg.customer("The refreshed dashboard looks fantastic. Keep it up!"));

            seeder.create("Two-factor auth not working", "secure@example.com", "Dana L",
                    TicketStatus.OPEN, cat.get("Account"), true,
                    Msg.customer("My authenticator codes are being rejected."),
                    Msg.admin("This usually means your device clock has drifted. Can you enable automatic time sync and retry?"));

            seeder.create("How do I cancel my subscription?", "leaving@example.com", "Pat M",
                    TicketStatus.PENDING_CUSTOMER, cat.get("Billing"), false,
                    Msg.customer("I need to cancel before the next renewal."),
                    Msg.ai("You can cancel any time under Settings > Billing > Cancel plan. Your access continues until the end of the current period."));

            seeder.create("General question about SSO", "it@bigco.example", "BigCo IT",
                    TicketStatus.NEW, cat.get("General"), false,
                    Msg.customer("Do you support SAML SSO on the business plan?"));

            seeder.create("Duplicate tickets from one email", "ops@support.example", "Ops",
                    TicketStatus.OPEN, cat.get("Technical"), true,
                    Msg.customer("Sometimes one customer email creates two tickets. Can you look into it?"),
                    Msg.admin("Thanks for flagging — we're reviewing the threading logic and will follow up."));

            log.info("Seeded {} sample tickets for the demo dashboard.", ticketRepository.count());
        };
    }

    private Map<String, Category> seedCategories(CategoryRepository categoryRepository) {
        if (categoryRepository.count() == 0) {
            List<Category> defaults = List.of(
                    Category.builder().name("Billing").color("#d97706").build(),
                    Category.builder().name("Technical").color("#4f46e5").build(),
                    Category.builder().name("Account").color("#2563eb").build(),
                    Category.builder().name("General").color("#64748b").build(),
                    Category.builder().name("Feedback").color("#059669").build());
            categoryRepository.saveAll(defaults);
            log.info("Seeded {} default categories.", defaults.size());
        }
        return categoryRepository.findAllByOrderByNameAsc().stream()
                .collect(Collectors.toMap(Category::getName, c -> c));
    }

    /** Small helper that persists a ticket plus its message thread, then backdates timestamps. */
    private record TicketSeeder(
            TicketRepository ticketRepository,
            TicketMessageRepository messageRepository,
            JdbcTemplate jdbcTemplate,
            Admin admin,
            Random random) {

        void create(String subject, String email, String name, TicketStatus status,
                Category category, boolean assigned, Msg... messages) {
            Ticket ticket = ticketRepository.save(Ticket.builder()
                    .subject(subject)
                    .customerEmail(email)
                    .customerName(name)
                    .status(status)
                    .category(category)
                    .assignedAdmin(assigned ? admin : null)
                    .build());

            List<TicketMessage> thread = new ArrayList<>();
            for (Msg m : messages) {
                thread.add(TicketMessage.builder()
                        .ticket(ticket)
                        .direction(m.direction())
                        .authorType(m.authorType())
                        .authorAdmin(m.fromAdmin() ? admin : null)
                        .body(m.body())
                        .build());
            }
            List<TicketMessage> saved = messageRepository.saveAll(thread);
            backdate(ticket, saved);
        }

        /**
         * createdAt/updatedAt are Hibernate-managed (updatable = false), so this bypasses
         * JPA with raw SQL updates to spread seeded timestamps across the last ~14 days —
         * otherwise every seeded ticket lands in the same instant and dashboard trend
         * charts show a single spike instead of real variation. Message order (inbound
         * before any outbound reply) is preserved, with realistic reply gaps in between,
         * so the average-response-time metric comes out sensible too.
         */
        private void backdate(Ticket ticket, List<TicketMessage> messages) {
            Instant now = Instant.now();
            Instant ticketCreatedAt = now
                    .minus(Duration.ofDays(random.nextInt(14)))
                    .minus(Duration.ofHours(random.nextInt(24)))
                    .minus(Duration.ofMinutes(random.nextInt(60)));

            List<Instant> messageTimes = new ArrayList<>();
            Instant cursor = ticketCreatedAt.plus(Duration.ofMinutes(random.nextInt(5)));
            for (int i = 0; i < messages.size(); i++) {
                if (i > 0) {
                    cursor = cursor.plus(Duration.ofMinutes(15 + random.nextInt(8 * 60)));
                }
                if (cursor.isAfter(now)) {
                    cursor = now;
                }
                messageTimes.add(cursor);
            }
            Instant updatedAt = messageTimes.get(messageTimes.size() - 1);

            jdbcTemplate.update("UPDATE tickets SET created_at = ?, updated_at = ? WHERE id = ?",
                    Timestamp.from(ticketCreatedAt), Timestamp.from(updatedAt), ticket.getId());
            for (int i = 0; i < messages.size(); i++) {
                jdbcTemplate.update("UPDATE ticket_messages SET created_at = ? WHERE id = ?",
                        Timestamp.from(messageTimes.get(i)), messages.get(i).getId());
            }
        }
    }
}
