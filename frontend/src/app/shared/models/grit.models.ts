export interface Metric {
  label: string;
  value: number;
  display: string;
}

export interface VerificationItem {
  number: string;
  title: string;
  description: string;
  isLast?: boolean;
}

export interface NavLink {
  label: string;
  href: string;
}
