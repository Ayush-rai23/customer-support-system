# Customer Support System

An AI-assisted customer support system where customer emails are automatically converted into tickets, categorized, and (when confident) auto-responded to using a knowledge base — with human admins handling anything the AI can't.

Admin-only system: customers never log in, they only ever interact via email.

## Status

MVP in planning/early scaffolding. See [PROJECT_PLAN.md](./PROJECT_PLAN.md) for the full problem statement, ticket statuses, MVP feature list, and out-of-scope items.

## Tech Stack

See [tech-stack.md](./tech-stack.md) for details.

- **Backend**: Spring Boot (Java), Spring Data JPA, PostgreSQL
- **Frontend**: React + Vite, Tailwind CSS
- **AI**: Spring AI (ticket categorization + knowledge-base-driven auto-response)

## Project Structure

```
.
├── PROJECT_PLAN.md   # Problem, solution, ticket statuses, MVP scope
├── tech-stack.md      # Chosen tech stack and rationale
├── backend/           # Spring Boot API (to be scaffolded)
└── frontend/          # React + Vite admin SPA (to be scaffolded)
```

## Getting Started

Setup instructions will be added here once the backend and frontend projects are scaffolded.
