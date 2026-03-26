import { Component } from '@angular/core';

@Component({
  selector: 'app-grit-loader',
  standalone: true,
  templateUrl: './grit-loader.html',
  styles: [`
    :host { display: contents; }

    @keyframes drawIn {
      to { stroke-dashoffset: 0; }
    }

    @keyframes fadeLoader {
      0%   { opacity: 1; }
      85%  { opacity: 1; }
      100% { opacity: 0; }
    }

    .loader-wrap {
      animation: fadeLoader 2.2s ease-in-out forwards;
    }

    .seg {
      animation: drawIn 0.45s cubic-bezier(0.4, 0, 0.2, 1) forwards;
      stroke-dashoffset: var(--l);
      stroke-dasharray: var(--l);
    }

    /* G */
    .g-top    { --l: 53px; animation-delay: 0s;    stroke: #111; }
    .g-left   { --l: 64px; animation-delay: 0.07s; stroke: #111; }
    .g-bot    { --l: 53px; animation-delay: 0.14s; stroke: #111; }
    .g-right  { --l: 32px; animation-delay: 0.19s; stroke: #111; }
    .g-mid    { --l: 26px; animation-delay: 0.24s; stroke: #2ED38D; }

    /* R */
    .r-left   { --l: 64px; animation-delay: 0.32s; stroke: #111; }
    .r-top    { --l: 44px; animation-delay: 0.38s; stroke: #111; }
    .r-rtop   { --l: 32px; animation-delay: 0.44s; stroke: #111; }
    .r-mid    { --l: 44px; animation-delay: 0.48s; stroke: #2ED38D; }
    .r-leg    { --l: 50px; animation-delay: 0.53s; stroke: #111; }

    /* I */
    .i-vert   { --l: 64px; animation-delay: 0.62s; stroke: #111; }
    .i-top    { --l: 40px; animation-delay: 0.67s; stroke: #2ED38D; }
    .i-bot    { --l: 40px; animation-delay: 0.72s; stroke: #2ED38D; }

    /* T */
    .t-top    { --l: 80px; animation-delay: 0.80s; stroke: #111; }
    .t-vert   { --l: 64px; animation-delay: 0.86s; stroke: #111; }
  `],
})
export class GritLoaderComponent {}
