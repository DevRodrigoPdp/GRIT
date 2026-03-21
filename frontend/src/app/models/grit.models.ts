export interface Metric {
  label: string;
  value: number; // 0–100
  display: string; // e.g. "84.2%"
}

export interface VerificationItem {
  number: string;   // "01", "02", "03"
  title: string;
  description: string;
  isLast?: boolean;
}

export interface NavLink {
  label: string;
  href: string;
}
