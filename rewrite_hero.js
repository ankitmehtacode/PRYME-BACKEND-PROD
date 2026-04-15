const fs = require('fs');
const file = '/Users/manishmehta/Documents/PRYME-BACKEND-PROD/Pryme-Frontend/src/components/home/HeroSection.tsx';
let data = fs.readFileSync(file, 'utf8');

// Add imports
data = data.replace('import { motion, AnimatePresence, Variants, useInView } from "framer-motion";', 
`import { motion, AnimatePresence, Variants, useInView } from "framer-motion";
import { useQuery } from "@tanstack/react-query";
import { PrymeAPI } from "@/lib/api";`);


// find the HeroSection component
const heroSectionStart = 'const HeroSection = memo(() => {';
const insertQuery = `
  const { data: dynamicOffers = [] } = useQuery({
    queryKey: ["public_hero_offers"],
    queryFn: () => PrymeAPI.getHeroOffers().then(res => res.data || res)
  });

  const activeOffers = dynamicOffers.length > 0 ? dynamicOffers.map((offer: any, i: number) => {
    // Map existing visual aesthetic strictly based on index
    const baseVisual = initialOffers[i % initialOffers.length];
    return {
      ...baseVisual,
      title: offer.title || baseVisual.title,
      bank: offer.lenderName || baseVisual.bank,
      tag: offer.tag || baseVisual.tag || "HOT RATE",
      highlights: offer.desc ? offer.desc.split('|').map((s: string) => s.trim()) : baseVisual.highlights
    };
  }) : initialOffers;

  const activeIndex = Math.abs(page % activeOffers.length);
  const offer = activeOffers[activeIndex];
`;

data = data.replace(
`const HeroSection = memo(() => {
  const [[page, direction], setPage] = useState([0, 0]);
  const [isAutoPlaying, setIsAutoPlaying] = useState(true);
  const heroRef = useRef<HTMLElement>(null);
  const isInView = useInView(heroRef, { once: false, margin: "0px 0px 200px 0px" });

  const activeIndex = Math.abs(page % initialOffers.length);
  const offer = initialOffers[activeIndex];`, 
`const HeroSection = memo(() => {
  const [[page, direction], setPage] = useState([0, 0]);
  const [isAutoPlaying, setIsAutoPlaying] = useState(true);
  const heroRef = useRef<HTMLElement>(null);
  const isInView = useInView(heroRef, { once: false, margin: "0px 0px 200px 0px" });
` + insertQuery);

data = data.replace(
  `{initialOffers.map((_, i) => (`,
  `{activeOffers.map((_, i) => (`
);

fs.writeFileSync(file, data);
