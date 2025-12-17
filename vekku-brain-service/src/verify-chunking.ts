import { BrainLogic } from './services/brain-logic/BrainLogic';

async function run() {
    const logic = BrainLogic.getInstance();

    console.log("⏳ Initializing Brain...");
    // We need to initialize to load the model
    await logic.initialize();

    // User's provided text
    const text = `The Modern Stack: Balancing Code, Capital, and Culinary Arts
In the fast-paced world of the 21st century, the definition of a well-rounded professional has shifted. It is no longer enough to specialize in a single silo; the modern polymath navigates a complex ecosystem of high-level systems architecture, volatile financial markets, and the grounding rituals of domestic life. From the MacBook on our desks to the Italian dishes on our dinner tables, here is a look at the intersections of our daily "stack."

The Engineering Foundation
At the core of the digital economy lies Education in Software Engineering. The landscape of Programming has bifurcated into distinct but complementary philosophies. On the backend, enterprise stability still relies heavily on Java, particularly when paired with robust Frameworks like Spring Boot. These tools allow architects to focus on critical System Design Concepts, ensuring Scalability through efficient Caching strategies and load balancing.

However, the modern web is increasingly dynamic. Python has surged in popularity due to its versatility in data and scripting, while TypeScript has brought type safety to the chaotic world of JavaScript. On the frontend, React remains the dominant library, allowing developers to build reactive user interfaces that feel instantaneous.

The Hardware Ecosystem
To build these systems, the hardware of choice has solidified around specific Brands. Technology professionals often gravitate toward Apple products for their Unix-based architecture and ecosystem integration. The MacBook Pro is the standard-issue tool for deployment, while the iPhone serves as the remote control for our digital lives.

This seamlessness is powered by Services like iCloud, which synchronizes our workspaces, notes, and communications across devices. It represents a user-experience philosophy that mirrors the very software principles developers strive for: reliability and invisibility.

The Financial Pulse
While we build value through code, we preserve it through financial literacy. Following Business News is essential for understanding where the capital flows. The Markets are currently a tale of two worlds. In traditional Stocks, the Tech Sector continues to drive growth, though shrewd investors often hedge their portfolios with the stability of the Energy Sector.

Simultaneously, the frontier of finance—Crypto—remains a topic of heated debate. Whether one views Bitcoin as "digital gold" or Ethereum as the foundation for decentralized computing, these assets have become impossible to ignore in a diversified portfolio.

The Analog Respite: Food and Cuisine
After a day spent analyzing Scalability and watching volatile Markets, the human brain requires an analog reset. This is where Food plays a crucial role. Cooking offers a tactile, sensory experience that coding cannot provide.

Italian Cuisine is a favorite for the technical mind because it relies on precision and high-quality ingredients. Mastering Pasta Dishes is surprisingly similar to debugging code. A true Carbonara, for example, requires strict timing to ensure the egg emulsion creates a sauce rather than scrambled eggs. A Lasagna is an exercise in layering and structure, much like a well-architected application.

The foundation of these dishes lies in the Ingredients. While heavy sauces comfort the soul, balance is found in Fruit. The acidity of Citrus fruits like Lemon and Orange can cut through rich flavors or serve as a palate cleanser. Meanwhile, Tree Fruit varieties such as the crisp Apple or the subtle Pear offer a natural sweetness that ends the day on a refreshing note, preparing the mind for the challenges of tomorrow.`;

    // console.log("\n📝 Input text:", text.substring(0, 50) + "...");
    // console.log("-----------------------------------");

    // Access private method using 'any' casting to bypass TS compiler check for verification
    // We use a threshold of 0.5 for clear separation
    const chunks = await (logic as any).semanticTextSplit(text, 0.45);

    console.log(`\n✅ Generated ${chunks.length} chunks:`);
    chunks.forEach((chunk: string, i: number) => {
        console.log(`\n[Chunk ${i + 1}]`);
        console.log(`"${chunk.trim()}"`);
    });
}

run().catch(e => {
    console.error("❌ Error:", e);
    process.exit(1);
});
