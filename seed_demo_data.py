#!/usr/bin/env python3
"""
seed_demo_data.py
=================
Origin Personal AI Assistant - Comprehensive Demo Data Seeder.

This script populates Origin with a rich, realistic demo persona for:
  Name      : Bhumi (Senior Product Designer)
  Pet Dog   : Leo (Golden Retriever)
  Loves     : Cake (Chocolate Truffle), Cats (British Shorthairs)
  Habits    : Daily Yoga at 7 AM, Meetings after 2 PM only
  Project   : Leading Project Aurora (Mobile App UX Redesign)

It seeds:
  1. Durable Memories (Identity, Pets, Food & Pet Preferences, Work Habits)
  2. Calendar Schedule (Design reviews, Team demos, Usability tests)
  3. Actionable Tasks (Figma mockups, Cake order, Vet checkup)
  4. Time-Sensitive Reminders & Armed Alarms
  5. Knowledge Base Document (Project Aurora Design Brief & Specs)
  6. Demo Chat Session with contextual conversation history

Usage:
  python seed_demo_data.py
  python seed_demo_data.py --clear          # Clears previous demo items before seeding
  python seed_demo_data.py --url http://localhost:8080/api
"""

import sys
import os
import json
import urllib.request
import urllib.error
from datetime import datetime, timedelta, timezone

BASE_URL = "http://localhost:8080/api"

# ── ANSI Terminal Colors ───────────────────────────────────────────────────────
GREEN = "\033[92m"
BLUE = "\033[94m"
CYAN = "\033[96m"
YELLOW = "\033[93m"
BOLD = "\033[1m"
RESET = "\033[0m"


def log(msg, color=RESET):
    print(f"{color}{msg}{RESET}")


def api_request(endpoint, method="GET", data=None):
    url = f"{BASE_URL}{endpoint}"
    headers = {"Content-Type": "application/json", "Accept": "application/json"}
    body = json.dumps(data).encode("utf-8") if data is not None else None

    req = urllib.request.Request(url, data=body, headers=headers, method=method)
    try:
        with urllib.request.urlopen(req, timeout=15) as resp:
            content = resp.read()
            if not content:
                return True, {}
            return True, json.loads(content.decode("utf-8"))
    except urllib.error.HTTPError as e:
        err_msg = e.read().decode("utf-8", errors="replace")
        return False, f"HTTP {e.code}: {err_msg}"
    except Exception as e:
        return False, str(e)


def upload_multipart(endpoint, filename, content_bytes):
    url = f"{BASE_URL}{endpoint}"
    boundary = "----OriginFormBoundary7MA4YWxkTrZu0gW"
    headers = {
        "Content-Type": f"multipart/form-data; boundary={boundary}",
        "Accept": "application/json",
    }

    body = (
        f"--{boundary}\r\n"
        f'Content-Disposition: form-data; name="file"; filename="{filename}"\r\n'
        f"Content-Type: text/markdown\r\n\r\n"
    ).encode("utf-8") + content_bytes + f"\r\n--{boundary}--\r\n".encode("utf-8")

    req = urllib.request.Request(url, data=body, headers=headers, method="POST")
    try:
        with urllib.request.urlopen(req, timeout=20) as resp:
            content = resp.read().decode("utf-8")
            return True, json.loads(content)
    except Exception as e:
        return False, str(e)


def check_health():
    log("\n[1/7] Checking connection to Origin backend...", BOLD)
    ok, res = api_request("/dashboard")
    if not ok:
        log(f"  ❌ Cannot connect to backend at {BASE_URL}.", YELLOW)
        log("     Make sure your Spring Boot backend is running (`mvn spring-boot:run` or IDE).", YELLOW)
        log("     Error details: " + str(res), RESET)
        sys.exit(1)
    log(f"  ✅ Backend is online and responding at {BASE_URL}!", GREEN)


def clear_existing():
    log("\n[Optional] Clearing previous demo items...", BOLD)
    api_request("/dashboard/clear", method="DELETE")
    log("  🧹 Dashboard items cleared for clean demo slate.", GREEN)


def seed_memories():
    log("\n[2/7] Seeding Durable User Memories (Bhumi's Persona)...", BOLD)
    memories = [
        ("My name is Bhumi, and I work as a Senior Product Designer at Acme Labs.", "IDENTITY"),
        ("I like cake, especially dark chocolate truffle and red velvet cake.", "PREFERENCE"),
        ("I love cats, especially British Shorthairs and rescue tabbies.", "PREFERENCE"),
        ("My dog's name is Leo; he is a 2-year-old playful Golden Retriever.", "PERSONAL"),
        ("My morning coffee order is an oat milk cappuccino with a dash of cinnamon and no sugar.", "PREFERENCE"),
        ("I prefer scheduling team syncs and meetings after 2:00 PM so I have deep work focus in the mornings.", "WORK"),
        ("I practice morning Vinyasa yoga and meditation every weekday from 7:00 AM to 7:45 AM.", "HABIT"),
        ("I am vegetarian and enjoy Italian and Thai cuisine.", "PREFERENCE"),
        ("I am currently leading the UX redesign for Project Aurora (mobile app v2.0).", "PROJECT"),
        ("My manager is Marcus Vance (Head of Design).", "WORK"),
    ]

    count = 0
    for fact, cat in memories:
        ok, res = api_request("/memories", method="POST", data={"factText": fact, "category": cat})
        if ok:
            count += 1
            print(f"  🧠 [{cat}] {fact[:60]}...")
        else:
            print(f"  ⚠️ Failed memory: {fact[:40]} -> {res}")
    log(f"  ✅ Seeded {count}/{len(memories)} persistent memories.", GREEN)


def seed_schedule():
    log("\n[3/7] Seeding Calendar Events (Bhumi's Meetings)...", BOLD)
    now = datetime.now(timezone.utc)

    # Dynamic times relative to current local execution
    events = [
        {
            "title": "Project Aurora UX Design Review",
            "description": "Review mobile onboarding prototypes and user flow with engineering leads.",
            "startTime": (now.replace(hour=9, minute=0, second=0)).isoformat(),
            "endTime": (now.replace(hour=10, minute=0, second=0)).isoformat(),
            "location": "Design Studio A / Zoom Link",
        },
        {
            "title": "1:1 Sync with Design Lead (Marcus)",
            "description": "Weekly priorities check-in, career growth, and Project Aurora timeline review.",
            "startTime": (now.replace(hour=11, minute=0, second=0)).isoformat(),
            "endTime": (now.replace(hour=11, minute=30, second=0)).isoformat(),
            "location": "Meeting Pod 4B",
        },
        {
            "title": "Product Roadmap Alignment Sync",
            "description": "Quarterly feature review with Product Managers and stakeholders.",
            "startTime": (now + timedelta(days=1)).replace(hour=9, minute=30, second=0).isoformat(),
            "endTime": (now + timedelta(days=1)).replace(hour=10, minute=30, second=0).isoformat(),
            "location": "Main Auditorium & Hybrid",
        },
        {
            "title": "Usability Testing Session (Customer Cohort #4)",
            "description": "Moderated user feedback session testing the new checkout flow.",
            "startTime": (now + timedelta(days=2)).replace(hour=10, minute=0, second=0).isoformat(),
            "endTime": (now + timedelta(days=2)).replace(hour=11, minute=0, second=0).isoformat(),
            "location": "User Research Lab",
        },
        {
            "title": "Design Team Weekly Retrospective & Demo Showcase",
            "description": "Share weekly wins and demonstrate interactive micro-interaction prototypes.",
            "startTime": (now + timedelta(days=4)).replace(hour=11, minute=30, second=0).isoformat(),
            "endTime": (now + timedelta(days=4)).replace(hour=12, minute=30, second=0).isoformat(),
            "location": "Social Lounge & Virtual",
        },
    ]

    count = 0
    for evt in events:
        ok, res = api_request("/dashboard/events", method="POST", data=evt)
        if ok:
            count += 1
            print(f"  📅 {evt['title']} ({evt['location']})")
        else:
            print(f"  ⚠️ Failed event: {evt['title']} -> {res}")
    log(f"  ✅ Seeded {count}/{len(events)} calendar events.", GREEN)


def seed_tasks():
    log("\n[4/7] Seeding Active Tasks (Bhumi's Action Items)...", BOLD)
    now = datetime.now(timezone.utc)

    tasks = [
        {
            "title": "Finalize high-fidelity Figma mockups for Project Aurora",
            "description": "Complete dark/light theme component variations and hand off to iOS team.",
            "priority": "URGENT",
            "status": "PENDING",
            "dueDate": (now + timedelta(hours=4)).isoformat(),
        },
        {
            "title": "Order chocolate truffle birthday cake for Mom",
            "description": "Pre-order 1kg Dutch chocolate truffle cake from Le Gateau bakery.",
            "priority": "HIGH",
            "status": "PENDING",
            "dueDate": (now + timedelta(days=1)).replace(hour=12, minute=0).isoformat(),
        },
        {
            "title": "Schedule Leo's annual vet checkup & vaccination",
            "description": "Call Paws & Claws clinic for Leo's 2-year vaccination booster.",
            "priority": "MEDIUM",
            "status": "PENDING",
            "dueDate": (now + timedelta(days=3)).replace(hour=15, minute=0).isoformat(),
        },
        {
            "title": "Prepare presentation slides for Monday manager sync",
            "description": "Summarize user testing metrics and accessibility audit score (WCAG AAA).",
            "priority": "HIGH",
            "status": "PENDING",
            "dueDate": (now + timedelta(days=5)).replace(hour=10, minute=0).isoformat(),
        },
        {
            "title": "Review design system token documentation for Android",
            "description": "Ensure color tokens match CSS variables and warm ivory palette.",
            "priority": "LOW",
            "status": "PENDING",
            "dueDate": (now + timedelta(days=6)).replace(hour=17, minute=0).isoformat(),
        },
        {
            "title": "Buy cat treats and wet food for animal shelter donation",
            "description": "Delivered to Happy Paws rescue center.",
            "priority": "LOW",
            "status": "COMPLETED",
            "dueDate": (now - timedelta(days=1)).isoformat(),
        },
    ]

    count = 0
    for t in tasks:
        ok, res = api_request("/dashboard/tasks", method="POST", data=t)
        if ok:
            count += 1
            print(f"  ✅ [{t['priority']}] {t['title']}")
        else:
            print(f"  ⚠️ Failed task: {t['title']} -> {res}")
    log(f"  ✅ Seeded {count}/{len(tasks)} tasks.", GREEN)


def seed_reminders_and_alarms():
    log("\n[5/7] Seeding Reminders & Alarms...", BOLD)
    now = datetime.now(timezone.utc)

    reminders = [
        {
            "message": "Drink water and do quick postural stretches",
            "remindAt": (now + timedelta(minutes=25)).isoformat(),
        },
        {
            "message": "Feed Leo and take him for an evening park walk",
            "remindAt": (now + timedelta(hours=3)).isoformat(),
        },
        {
            "message": "Pick up chocolate truffle cake from bakery",
            "remindAt": (now + timedelta(days=1)).replace(hour=13, minute=0).isoformat(),
        },
        {
            "message": "Submit weekly timesheet and design sprint report",
            "remindAt": (now + timedelta(days=4)).replace(hour=16, minute=30).isoformat(),
        },
    ]

    for rem in reminders:
        ok, res = api_request("/dashboard/reminders", method="POST", data=rem)
        if ok:
            print(f"  🔔 Reminder: {rem['message']}")

    alarms = [
        {
            "label": "Morning Yoga & Mindfulness Practice",
            "triggerAt": (now + timedelta(days=1)).replace(hour=7, minute=0, second=0).isoformat(),
        },
        {
            "label": "Project Aurora Demo Prep",
            "triggerAt": (now + timedelta(hours=2, minutes=15)).isoformat(),
        },
    ]

    for al in alarms:
        ok, res = api_request("/dashboard/alarms", method="POST", data=al)
        if ok:
            print(f"  ⏰ Alarm: {al['label']}")

    log(f"  ✅ Seeded {len(reminders)} reminders and {len(alarms)} armed alarms.", GREEN)


def seed_knowledge_base():
    log("\n[6/7] Creating & Ingesting Demo Knowledge Base Document...", BOLD)
    doc_content = """# Project Aurora: Mobile App UX Redesign Brief

## 1. Executive Summary
Project Aurora is a comprehensive user experience overhaul of the Acme flagship mobile application. The initiative focuses on visual clarity, responsive interaction models, and intuitive task completion.

- **Lead Product Designer**: Bhumi
- **Product Manager**: Sarah Jenkins
- **Target Launch Window**: Q4 2026
- **Status**: High-Fidelity Prototyping & Usability Validation

## 2. Core Design Principles
1. **Calm & Editorial Aesthetic**: Warm ivory background palettes (#fbfaf8), lavender and periwinkle accents, and intentional whitespace.
2. **Accessible by Default**: Full WCAG 2.1 AAA color contrast compliance across light and dark modes.
3. **One-Handed Navigation**: Critical action buttons positioned within the natural thumb zone.

## 3. Key Personas & Target Audience
- **Primary Persona: The Focus-Driven Professional**
  - Needs rapid glanceable overviews of daily commitments.
  - Prefers deep focus blocks in mornings without ad-hoc meeting interruptions.
- **Secondary Persona: The Collaborative Team Lead**
  - Coordinates cross-functional reviews and design handoffs.

## 4. Key Milestones & Roadmap
- **Sprint 1-2**: Design system tokens and atomic component library (Completed).
- **Sprint 3-4**: Mobile onboarding, profile creation, and interactive timeline (In Review).
- **Sprint 5-6**: Real-time alert notifications and end-to-end user testing.

## 5. Team Contacts
- Design Lead: Bhumi (bhumi@acmelabs.internal)
- Engineering Lead: David Chen (david.chen@acmelabs.internal)
"""

    # 1. Write file to local disk (data/documents)
    docs_dir = os.path.join(os.path.dirname(__file__), "data", "documents")
    os.makedirs(docs_dir, exist_ok=True)
    doc_path = os.path.join(docs_dir, "project_aurora_design_brief.md")
    with open(doc_path, "w", encoding="utf-8") as f:
        f.write(doc_content)
    print(f"  📄 Written locally to: {doc_path}")

    # 2. Upload via API for immediate vector chunking & indexing
    ok, res = upload_multipart(
        "/documents/upload",
        "project_aurora_design_brief.md",
        doc_content.encode("utf-8"),
    )
    if ok:
        print(f"  🚀 Ingested into pgvector RAG: {res.get('chunks', 0)} chunks indexed!")
        log("  ✅ Knowledge base ready for hybrid semantic search.", GREEN)
    else:
        print(f"  ⚠️ Document upload note: {res} (Local file is ready for boot scan).")


def seed_demo_conversation():
    log("\n[7/7] Creating Pre-populated Demo Chat Conversation...", BOLD)
    ok, session = api_request("/chat/session", method="POST")
    if not ok:
        print(f"  ⚠️ Could not create session: {session}")
        return

    session_id = session.get("sessionId")
    print(f"  💬 Session created: {session_id[:8]}... ('Project Aurora & Daily Planning')")

    # Send a sample prompt from Bhumi to show the assistant in action
    demo_prompt = "Hi Origin! Can you summarize my top priorities today and what you remember about my meeting preferences?"
    print(f'  👤 Bhumi: "{demo_prompt}"')
    ok, response_text = api_request("/chat", method="POST", data={"sessionId": session_id, "message": demo_prompt})
    if ok:
        print(f"  🤖 Origin replied ({len(str(response_text))} chars).")
        log("  ✅ Demo conversation initialized and ready in UI.", GREEN)
    else:
        print(f"  ⚠️ Chat turn response: {response_text}")


def print_walkthrough_guide():
    print("\n" + "=" * 70)
    log("  🎉 DEMO DATA SEEDED SUCCESSFULLY FOR 'BHUMI'!", GREEN + BOLD)
    print("=" * 70)
    print(
        f"""
{CYAN}Here is the walkthrough script the developer can use to show their manager:{RESET}

1. {BOLD}Durable Memory Bank Demo{RESET}:
   • Navigate to {BOLD}Memory Bank{RESET} view in the left sidebar.
   • Show that Origin remembers Bhumi's persona:
     - Name: {GREEN}Bhumi{RESET}
     - Favorite food: {GREEN}Chocolate truffle & red velvet cake{RESET}
     - Pets: {GREEN}Dog named Leo (Golden Retriever){RESET}, loves cats
     - Meeting habits: {GREEN}Prefers meetings after 2 PM{RESET}
     - Habits: {GREEN}Yoga at 7 AM{RESET}

2. {BOLD}Interactive Right Sidebar & Schedule Demo{RESET}:
   • Switch to {BOLD}Assistant Chat{RESET} view.
   • Look at the {BOLD}Right Sidebar{RESET}:
     - ⚡ Spotlight Hero Card highlights the next upcoming item.
     - 🔔 Reminders dropdown shows "Drink water", "Feed Leo", etc.
     - ⏰ Alarms dropdown shows 7:00 AM Yoga and Demo Prep alarms.
     - ✅ Tasks dropdown shows prioritized items (Figma mockups, cake order).
     - Test the {BOLD}Today / Urgent / All{RESET} filter pills.
     - Click the checkmark to complete or dismiss an item.

3. {BOLD}Multi-Turn AI Chat Prompts to Test Live{RESET}:
   Try asking Origin these prompts in the chat:
   • {YELLOW}"What do you remember about my personal preferences and pets?"{RESET}
     → Origin will recall Bhumi, Leo the dog, cats, and chocolate truffle cake!
   • {YELLOW}"Plan my day considering my meeting preferences."{RESET}
     → Origin will inspect the schedule, keeping mornings free for design work!
   • {YELLOW}"What is Project Aurora and what are its core design principles?"{RESET}
     → Origin will retrieve facts from the ingested markdown document via RAG!
   • {YELLOW}"Set a reminder to buy cake for Leo's birthday tomorrow at 3 PM"{RESET}
     → Origin will execute the reminder tool relative to the real current time!
"""
    )
    print("=" * 70)


def main():
    global BASE_URL
    args = sys.argv[1:]
    should_clear = False

    for i, arg in enumerate(args):
        if arg == "--clear":
            should_clear = True
        elif arg == "--url" and i + 1 < len(args):
            BASE_URL = args[i + 1].rstrip("/")

    print("=" * 70)
    log("  ORIGIN AI ASSISTANT — DEMO DATA GENERATOR", CYAN + BOLD)
    log(f"  Target URL: {BASE_URL}", BLUE)
    print("=" * 70)

    check_health()
    if should_clear:
        clear_existing()

    seed_memories()
    seed_schedule()
    seed_tasks()
    seed_reminders_and_alarms()
    seed_knowledge_base()
    seed_demo_conversation()
    print_walkthrough_guide()


if __name__ == "__main__":
    main()

