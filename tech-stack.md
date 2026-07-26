# Tech Stack

## Backend — Spring Boot
- REST API layer exposing endpoints for tickets, replies, categories, knowledge base uploads, and dashboard metrics.
- Email webhook endpoint to receive inbound emails and convert them into tickets/replies.
- Business logic for ticket threading, status transitions, and assignment.
- Spring AI for AI integration — ticket categorization and knowledge-base-driven auto-response generation.
- Outbound email sending (replies back to customers).
- Authentication & session/token handling for admin login.
- Data persistence via Spring Data JPA against PostgreSQL.

## Frontend — React + Vite
- Admin single-page application (SPA); no customer-facing pages, since customers only interact via email.
- Views: ticket inbox/queue, ticket detail (threaded conversation), knowledge base file upload, dashboard/metrics.
- Client-side routing for navigating between inbox, ticket detail, and dashboard.
- API integration layer to consume the Spring Boot REST endpoints.
- Component-level state for filters (status, category, assignee) and ticket thread rendering.

## Database — PostgreSQL
- Relational store for all application data (tickets, replies, categories, admins, knowledge base references).

## Styling — Tailwind CSS
- Utility-first styling for all admin UI screens (inbox, ticket thread, dashboard).
- Consistent, responsive layout for tables/lists (ticket queue) and chat-style threaded views (ticket detail).
- Used for dashboard chart containers, status badges/labels, and form styling (login, knowledge base upload).
