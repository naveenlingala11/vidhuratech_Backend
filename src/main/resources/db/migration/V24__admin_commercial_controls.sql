create table if not exists plan_pricing_controls (
                                                     id bigserial primary key,
                                                     plan_code varchar(50) not null unique,
    plan_name varchar(120) not null,
    price numeric(12,2) not null default 0,
    compare_at_price numeric(12,2),
    duration_days int not null default 30,
    company_limit int not null default 999,
    highlighted boolean not null default false,
    active boolean not null default true,
    updated_at timestamp default now()
    );

create table if not exists discount_controls (
                                                 id bigserial primary key,
                                                 code varchar(80) not null unique,
    title varchar(160) not null,
    discount_type varchar(20) not null,
    discount_value numeric(12,2) not null,
    plan_code varchar(50),
    max_uses int,
    used_count int not null default 0,
    starts_at timestamp,
    expires_at timestamp,
    active boolean not null default true,
    created_at timestamp default now(),
    updated_at timestamp default now()
    );

create table if not exists project_access_controls (
                                                       id bigserial primary key,
                                                       control_key varchar(100) not null unique,
    label varchar(180) not null,
    description text,
    enabled boolean not null default true,
    updated_at timestamp default now()
    );

insert into project_access_controls(control_key, label, description, enabled)
values
    ('PUBLIC_GUEST_PRACTICE', 'Guest Free Practice', 'Allow non-login users to register and attend free practice.', true),
    ('PREMIUM_CHALLENGES', 'Premium Challenges', 'Enable premium challenge locking and access checks.', true),
    ('CODING_CONTESTS', 'Coding Contests', 'Show public coding contest page and contest leaderboard.', true),
    ('PAYMENTS', 'Payments', 'Allow Razorpay checkout and plan activation.', true),
    ('DISCOUNTS', 'Discounts', 'Allow coupon based discounts on pricing plans.', true),
    ('PUBLIC_REGISTRATION', 'Public Registration', 'Allow new user registration and password setup flow.', true)
    on conflict (control_key) do nothing;

insert into plan_pricing_controls(plan_code, plan_name, price, compare_at_price, duration_days, company_limit, highlighted, active)
values
    ('STARTER', 'Starter', 499, 999, 30, 5, false, true),
    ('PRO', 'Pro', 1499, 2499, 30, 15, true, true),
    ('ELITE', 'Elite', 4999, 7999, 180, 999, false, true)
    on conflict (plan_code) do nothing;