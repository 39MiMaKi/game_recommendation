// 游戏标签分类
const tags = ['动作', '冒险', '角色扮演', '射击', '策略', '体育', '模拟', '解谜', '竞速', '格斗'];

// 生成随机价格
function getRandomPrice() {
  return (Math.random() * 100 + 5).toFixed(2);
}

// 生成随机折扣
function getRandomDiscount() {
  return Math.floor(Math.random() * 90);
}

// 生成随机标签(1-3个)
function getRandomTags() {
  const count = Math.floor(Math.random() * 3) + 1;
  const shuffled = [...tags].sort(() => 0.5 - Math.random());
  return shuffled.slice(0, count);
}

// 生成游戏数据
// 真实steam游戏数据
const realGames = [
  {
    name: "Counter-Strike 2",
    description: "Counter-Strike 2 是CS:GO的免费升级版，代表了技术的最前沿。",
    cover: "https://cdn.cloudflare.steamstatic.com/steam/apps/730/header.jpg",
    images: [
      "https://cdn.cloudflare.steamstatic.com/steam/apps/730/ss_34090867f1a02b6c1769ba9d9ad9e5dcf1a5be58.600x338.jpg",
      "https://cdn.cloudflare.steamstatic.com/steam/apps/730/ss_398259319c4b1afa0c4c2e7d68fd7f5b9a0f0a9b.600x338.jpg"
    ]
  },
  {
    name: "Dota 2",
    description: "Dota 2是一款免费的多人在线战斗竞技场游戏，由Valve开发。",
    cover: "https://cdn.cloudflare.steamstatic.com/steam/apps/570/header.jpg",
    images: [
      "https://cdn.cloudflare.steamstatic.com/steam/apps/570/ss_86d675fdc73ba10467abbc7af3c7af5602264f7a.600x338.jpg",
      "https://cdn.cloudflare.steamstatic.com/steam/apps/570/ss_3670ca0c370b05b5d6e3e8e8a5a3576f6e1a9a6f.600x338.jpg"
    ]
  }
  // 可以添加更多真实游戏数据
];

function generateGames(count) {
  const games = [];
  
  for (let i = 0; i < count; i++) {
    const price = getRandomPrice();
    const discount = getRandomDiscount();
    const finalPrice = (price * (1 - discount / 100)).toFixed(2);
    const gameIndex = i % realGames.length;
    
    games.push({
      appId: `app${1000 + i}`,
      name: realGames[gameIndex].name,
      description: realGames[gameIndex].description,
      price: price,
      discount: discount,
      finalPrice: finalPrice,
      tags: getRandomTags(),
      cover: realGames[gameIndex].cover,
      images: realGames[gameIndex].images,
      win: Math.random() > 0.3,
      mac: Math.random() > 0.5,
      linux: Math.random() > 0.7,
      status: Math.random() > 0.8 ? 1 : 0
    });
  }
  
  return games;
}

// 生成200条游戏数据
const games = generateGames(200);

// 导出数据
console.log(JSON.stringify(games, null, 2));
export default games;