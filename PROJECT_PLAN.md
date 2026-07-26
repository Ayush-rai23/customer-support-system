# Problem
Need to build a customer support management system for my business where customer emails are managed in a better way.

# Solution
- Customer sends email to our support address.
- Email is received via webhook (inbound email parsing) and converted into a ticket.
- Ticket is auto-categorized using AI.
- AI agent auto-responds using the knowledge base, when confident.
- If AI cannot confidently respond, ticket is escalated to a human admin.
- Admin logs into the system and replies; all replies (AI or human) are sent back to the customer via email and shown as a threaded conversation in the UI.
- Customers only ever interact via email — they have no login/access to the system. Only admins/agents use the system.

# Ticket Statuses (suggested)
- **New** — ticket created, not yet processed by AI.
- **Auto-Resolved** — AI responded and no further action expected.
- **Pending (Customer)** — waiting on customer reply.
- **Open / In Progress** — assigned to an admin, awaiting action.
- **Escalated** — AI could not confidently respond, needs human review.
- **Resolved** — issue closed out, resolution sent.
- **Closed** — ticket archived (auto-close after resolved + no reply within X days).
- **Spam** — flagged and excluded from normal queue/metrics.

# MVP Features

**Email Ingestion**
- Receive inbound emails via webhook.
- Parse sender, subject, body, attachments.
- Thread replies to the correct existing ticket (not create duplicates) using email headers/subject matching.

**Ticket Management**
- Create ticket from new email; append replies to existing ticket thread.
- View ticket as a threaded conversation (like a chat/email thread) in UI.
- Manual status change, manual category override, manual assignment to an admin.
- Attachment viewing.

**AI Categorization**
- Auto-tag ticket with a category on creation.
- Support re-categorization if ticket content changes (new reply).

**AI Auto-Response**
- Use knowledge base to draft/send a response automatically.
- Confidence threshold: below threshold → escalate to human instead of auto-sending.
- Clear rules for what AI must never auto-respond to (e.g. refunds/legal/complaints) — escalate these always.

**Knowledge Base**
- Admin uploads PDF/doc file(s) as the AI's reference source.
- AI reads from uploaded file(s) to answer/draft responses.
- Replacing/re-uploading a file updates the source (no in-app entry editor for MVP).

**Human Agent Workflow**
- Admin inbox/queue view of tickets (filter by status/category/assignee).
- Reply to ticket (sent as email to customer).
- Manual takeover of an AI-handled ticket.

**Dashboard / Metrics**
- Average response time.
- Total tickets per day.
- Tickets by category (daily breakdown).
- Tickets by status/bucket (counts).
- Basic charts/trend view over time.

**Auth & Security**
- Admin login (single system, admin-only access).
- Role/permission basics (e.g. admin vs agent) if multiple people will use it.
- Secure session handling.

# Out of Scope (MVP)
- Customer-facing portal or login — customers only interact via email, never access the system directly.
- Multi-channel support (chat, social media, phone) — email only.
- Advanced AI features: sentiment analysis, auto-escalation prediction, multilingual support.
- SLA enforcement/alerts (breach notifications, auto-escalation timers).
- Customer satisfaction (CSAT/NPS) surveys.
- Canned responses/macros library for agents.
- Advanced reporting/export (CSV export, custom report builder).
- Team/department routing rules beyond simple manual assignment.
- Third-party integrations (CRM, helpdesk tools, Slack notifications, etc.).
- Mobile app — web-based admin UI only.
- Audit logging/history of edits beyond basic ticket status/reply trail.
