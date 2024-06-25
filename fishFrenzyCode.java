// import statements 
import tester.*;
import javalib.worldimages.*;
import javalib.funworld.*;
import java.awt.Color;
import java.util.Random;

// abstract class that represents Fish
abstract class Fish {
  int x;
  int y;
  Color color;
  int radius;

  // Constructor
  Fish(int x, int y, Color color, int radius) {
    this.x = x;
    this.y = y;
    this.color = color;
    this.radius = radius;
  }

  // creates the fish body
  WorldImage makeFishBody() {
    CircleImage smallBlackCircle = new CircleImage(5, OutlineMode.SOLID, Color.BLACK);
    CircleImage mediumWhiteCircle = new CircleImage(10, OutlineMode.SOLID, Color.WHITE);
    CircleImage largeColoredCircle = new CircleImage(radius, OutlineMode.SOLID, this.color);
    OverlayOffsetImage eyeWithWhiteBackground = new OverlayOffsetImage(mediumWhiteCircle, 10, 0,
        largeColoredCircle);
    OverlayOffsetImage completeEye = new OverlayOffsetImage(smallBlackCircle, 10, 0,
        eyeWithWhiteBackground);
    EquilateralTriangleImage tailTriangle = new EquilateralTriangleImage(20, OutlineMode.SOLID,
        this.color);
    RotateImage rotatedTail = new RotateImage(tailTriangle, 270);
    BesideImage fishBody = new BesideImage(completeEye, rotatedTail);

    return fishBody;
  }

}

// class that represents the PlayerFish
class PlayerFish extends Fish {

  // Constructor
  PlayerFish(int x, int y, Color color, int radius) {
    super(x, y, color, radius);
  }

  // Moves the player fish
  PlayerFish move(String key) {
    int newX = this.x;
    int newY = this.y;

    if (key.equals("left")) {
      newX -= 10;
    }
    else if (key.equals("right")) {
      newX += 10;
    }
    else if (key.equals("up")) {
      newY -= 10;
    }
    else if (key.equals("down")) {
      newY += 10;
    }

    if (newX < 0) {
      newX = 600;
    }
    else if (newX > 600) {
      newX = 0;
    }

    if (newY < 0) {
      newY = 400;
    }
    else if (newY > 400) {
      newY = 0;
    }

    return new PlayerFish(newX, newY, this.color, this.radius);
  }

  // increases the player fish's radius by a given amount to increase the size
  PlayerFish grow(int amount) {
    return new PlayerFish(this.x, this.y, this.color, this.radius + amount);
  }

  // checks to see if 2 fishes have collided
  boolean checkCollision(BackgroundFish other) {
    int dx = this.x - other.x;
    int dy = this.y - other.y;
    double distance = Math.sqrt(dx * dx + dy * dy);
    return distance < other.calculateRadius(this);
  }

  // compares radius of player and background fishes
  boolean radiusCompare(int bg) {
    return this.radius > bg;

  }

  // compares radius of player and background fishes using a different statement
  boolean radiusCompareTwo(int bg) {
    return this.radius <= bg;

  }

  // places the player in the world
  WorldScene addPlayerInScene() {
    WorldScene scene = new WorldScene(600, 400);
    return scene.placeImageXY(this.makeFishBody(), this.x, this.y);
  }

}

// represents a background fish
class BackgroundFish extends Fish {
  Random rand;

  // Constructor
  BackgroundFish(int x, int y, Color color, int radius) {
    super(x, y, color, radius);
  }

  // Constructor 2
  BackgroundFish(int x, int y, Color color, int radius, Random rand) {
    super(x, y, color, radius);
    this.rand = rand;
  }

  // moves the background fish
  BackgroundFish move() {

    int moveDirection = (int) (Math.random() * 4);
    int moveStep = (int) (Math.random() * 15 + 15);

    int newX = this.x;
    int newY = this.y;

    if (moveDirection == 0) {
      newX += moveStep;
    }
    else if (moveDirection == 1) {
      newX -= moveStep;
    }
    else if (moveDirection == 2) {
      newY += moveStep;
    }
    else if (moveDirection == 3) {
      newY -= moveStep;
    }

    // Wraps around the screen if the fish goes out of bounds
    if (newX < 0) {
      newX = 600 + newX;
    }
    else if (newX > 600) {
      newX = newX - 600;
    }

    if (newY < 0) {
      newY = 400 + newY;
    }
    else if (newY > 400) {
      newY = newY - 400;
    }

    return new BackgroundFish(newX, newY, this.color, this.radius);
  }

  // compares the player and background fish radius
  public boolean checkRadiusLarger(PlayerFish player) {
    return player.radiusCompare(this.radius);
  }

  // checks wether the player fish has lost the game by hitting the larger fish
  boolean lose(PlayerFish other) {
    int dx = this.x - other.x;
    int dy = this.y - other.y;
    double distance = Math.sqrt(dx * dx + dy * dy);
    return (distance < this.calculateRadius(other)) && other.radiusCompareTwo(this.radius);
  }

  // calculates the radius of the background fish plus the playerfish
  int calculateRadius(PlayerFish other) {
    return this.radius + other.radius;
  }

  // draws the background fish on the worldscene
  WorldScene drawBackgroundFish(WorldScene scene) {
    return scene.placeImageXY(this.makeFishBody(), this.x, this.y);
  }
}

// represents a ILoFish
interface ILoFish {

  // moves all the background fish in the list
  ILoFish moveAll();

  // places all the background fish onto the scene
  WorldScene drawAll(WorldScene scene);

  // counts fishes that have been added to the game
  int countFishes();

  // adds a fish onto the game.
  ILoFish addFish(BackgroundFish newFish, int timer, int fishCount);

  // checks to see if all the fish in a list have collided with the player fish
  boolean checkAllCollisions(PlayerFish player);

  // removes fish from the list that have been eaten by the player.
  ILoFish removeEaten(PlayerFish player);

  // checks to see if the player won
  boolean hasWon(PlayerFish player);

  // checks to see if the player has lost
  boolean getLose(PlayerFish player);

}

// represents a ConsLoFish
class ConsLoFish implements ILoFish {
  BackgroundFish first;
  ILoFish rest;

  // Constructor
  ConsLoFish(BackgroundFish first, ILoFish rest) {
    this.first = first;
    this.rest = rest;
  }

  // moves all the background fish in the list
  public ILoFish moveAll() {
    return new ConsLoFish(this.first.move(), this.rest.moveAll());
  }

  // places all the background fish onto the scene
  public WorldScene drawAll(WorldScene scene) {
    return this.rest.drawAll(this.first.drawBackgroundFish(scene)); // go to different method in
    // background
  }

  // adds a fish onto the game.
  public ILoFish addFish(BackgroundFish newFish, int timer, int fishCount) {
    if (timer % 20 == 0 && fishCount < 10) {
      return new ConsLoFish(newFish, this);
    }
    else {
      return this;
    }
  }

  // checks to see if all the fish in a list have collided with the player fish
  public boolean checkAllCollisions(PlayerFish player) {
    if (player.checkCollision(this.first)) {
      return true;
    }
    return this.rest.checkAllCollisions(player);
  }

  // removes fish from the list that have been eaten by the player.
  public ILoFish removeEaten(PlayerFish player) {
    if (player.checkCollision(this.first) && this.first.checkRadiusLarger(player)) {
      return this.rest.removeEaten(player);
    }
    return new ConsLoFish(this.first, this.rest.removeEaten(player));
  }

  // checks to see if the player won
  public boolean hasWon(PlayerFish player) {
    return false;
  }

  // counts fishes that have been added to the game
  public int countFishes() {
    return 1 + this.rest.countFishes();
  }

  // checks to see if the player has lost
  public boolean getLose(PlayerFish player) {
    return this.first.lose(player) || this.rest.getLose(player);
  }

}

// empty fish class 
class MtLoFish implements ILoFish {

  // moves all the background fish in the list
  public ILoFish moveAll() {
    return this;
  }

  // places all the background fish onto the scene
  public WorldScene drawAll(WorldScene scene) {
    return scene;
  }

  // checks to see if all the fish in a list have collided with the player fish
  public boolean checkAllCollisions(PlayerFish player) {
    return false;
  }

  // removes fish from the list that have been eaten by the player.
  public ILoFish removeEaten(PlayerFish player) {
    return this;
  }

  // checks to see if the player won
  public boolean hasWon(PlayerFish player) {
    return true;
  }

  // adds a fish onto the game.
  public ILoFish addFish(BackgroundFish newFish, int timer, int fishCount) {
    if (timer % 20 == 0 && fishCount < 10) {
      return new ConsLoFish(newFish, this);
    }
    else {
      return this;
    }
  }

  // counts fishes that have been added to the game
  public int countFishes() {
    return 0;
  }

  // checks to see if the player has lost in a empty list scenario
  public boolean getLose(PlayerFish player) {
    return false;
  }
}

// represents a game class
class Game extends World {
  PlayerFish playerFish;
  ILoFish backgroundFish;
  int timer;

  // Constructor
  Game(PlayerFish playerFish, ILoFish backgroundFish, int timer) {
    this.playerFish = playerFish;
    this.backgroundFish = backgroundFish;
    this.timer = timer;
  }

  // Initializes the game
  public WorldScene makeScene() {
    return this.backgroundFish.drawAll(this.playerFish.addPlayerInScene());
  }

  // runs after every tick
  public World onTick() {
    this.timer++;
    int fishCount = this.backgroundFish.countFishes();
    ILoFish newFishList = this.backgroundFish.moveAll();
    int beforeEatingCount = newFishList.countFishes();
    ILoFish fishListAfterEating = newFishList.removeEaten(this.playerFish);
    int afterEatingCount = fishListAfterEating.countFishes();

    if (beforeEatingCount > afterEatingCount) {
      return new Game(this.playerFish.grow(5), fishListAfterEating, timer);
    }

    if (this.backgroundFish.hasWon(playerFish)) {
      return this.endOfWorld("You Won");
    }

    if (this.backgroundFish.getLose(playerFish)) {
      return this.endOfWorld("YOU LOSE");
    }

    return new Game(this.playerFish,
        newFishList.addFish(new BackgroundFish((int) (Math.random() * 600),
            (int) (Math.random() * 400), Color.PINK, (int) (Math.random() * 20 + 10)), timer,
            fishCount),
        timer);
  }

  /// shows the last scene of the game
  public WorldScene lastScene(String msg) {
    return new WorldScene(10, 10).placeImageXY(new TextImage(msg, 60, Color.RED), 200, 100);
  }

  // does something based on what key is pressed
  public Game onKeyEvent(String key) {
    return new Game(this.playerFish.move(key), this.backgroundFish, timer);
  }
}

// represents examples of the game
class ExamplesGame {

  ILoFish fishes = new ConsLoFish(new BackgroundFish(100, 100, Color.RED, 30),
      new ConsLoFish(new BackgroundFish(200, 150, Color.GREEN, 30),
          new ConsLoFish(new BackgroundFish(400, 250, Color.YELLOW, 10), new MtLoFish())));

  PlayerFish player = new PlayerFish(300, 300, Color.BLUE, 20);

  PlayerFish player2 = new PlayerFish(200, 200, Color.RED, 10);

  PlayerFish player3 = new PlayerFish(100, 100, Color.YELLOW, 30);

  BesideImage player1Image = new BesideImage(
      new OverlayOffsetImage(new CircleImage(5, OutlineMode.SOLID, Color.BLACK), 10, 0,
          new OverlayOffsetImage(new CircleImage(10, OutlineMode.SOLID, Color.WHITE), 10, 0,
              new CircleImage(20, OutlineMode.SOLID, Color.BLUE))),
      new RotateImage(new EquilateralTriangleImage(20, OutlineMode.SOLID, Color.BLUE), 270));

  BesideImage player2Image = new BesideImage(
      new OverlayOffsetImage(new CircleImage(5, OutlineMode.SOLID, Color.BLACK), 10, 0,
          new OverlayOffsetImage(new CircleImage(10, OutlineMode.SOLID, Color.WHITE), 10, 0,
              new CircleImage(10, OutlineMode.SOLID, Color.RED))),
      new RotateImage(new EquilateralTriangleImage(20, OutlineMode.SOLID, Color.RED), 270));

  BesideImage player3Image = new BesideImage(
      new OverlayOffsetImage(new CircleImage(5, OutlineMode.SOLID, Color.BLACK), 10, 0,
          new OverlayOffsetImage(new CircleImage(10, OutlineMode.SOLID, Color.WHITE), 10, 0,
              new CircleImage(30, OutlineMode.SOLID, Color.YELLOW))),
      new RotateImage(new EquilateralTriangleImage(20, OutlineMode.SOLID, Color.YELLOW), 270));

  PlayerFish playerMoveRight = new PlayerFish(310, 300, Color.BLUE, 20);

  PlayerFish playerMoveLeft = new PlayerFish(290, 300, Color.BLUE, 20);

  PlayerFish playerMoveUp = new PlayerFish(300, 290, Color.BLUE, 20);

  PlayerFish playerMoveDown = new PlayerFish(300, 310, Color.BLUE, 20);

  PlayerFish playerIncrease = new PlayerFish(300, 300, Color.BLUE, 30);

  PlayerFish playerDecrease = new PlayerFish(300, 300, Color.BLUE, 19);

  BackgroundFish nearFish = new BackgroundFish(310, 300, Color.RED, 10);

  BackgroundFish farFish = new BackgroundFish(500, 300, Color.RED, 10);

  WorldScene playerInScene = new WorldScene(600, 400).placeImageXY(player.makeFishBody(), 300, 300);

  WorldScene player2InScene = new WorldScene(600, 400).placeImageXY(player2.makeFishBody(), 200,
      200);

  WorldScene player3InScene = new WorldScene(600, 400).placeImageXY(player3.makeFishBody(), 100,
      100);

  BackgroundFish bgFish = new BackgroundFish(300, 300, Color.GREEN, 15);

  Game game1 = new Game(this.player, this.fishes, 0);

  WorldScene youWon = new WorldScene(10, 10).placeImageXY(new TextImage("You Won", 60, Color.RED),
      200, 100);
  WorldScene youLost = new WorldScene(10, 10).placeImageXY(new TextImage("You Lost", 60, Color.RED),
      200, 100);
  ILoFish empty = new MtLoFish();
  Game playerMoveRightInGame = new Game(this.playerMoveRight, this.empty, 0);
  Game playerMoveLeftInGame = new Game(this.playerMoveLeft, this.empty, 0);

  // Tests the bigbang function
  boolean testBigBang(Tester t) {
    Game game = new Game(new PlayerFish(300, 300, Color.BLUE, 20), fishes, 0);
    return game.bigBang(600, 400, 0.075);
  }

  // Tests Fish class methods
  boolean testmakeFishBody(Tester t) {
    return t.checkExpect(this.player.makeFishBody(), this.player1Image)
        && t.checkExpect(this.player2.makeFishBody(), this.player2Image)
        && t.checkExpect(this.player3.makeFishBody(), this.player3Image);
  }

  // Test player methods
  // Test PlayerFish movement logic
  boolean testPlayerFishMovement(Tester t) {
    return t.checkExpect(this.player.move("right"), this.playerMoveRight)
        && t.checkExpect(this.player.move("left"), this.playerMoveLeft)
        && t.checkExpect(this.player.move("up"), this.playerMoveUp)
        && t.checkExpect(this.player.move("down"), this.playerMoveDown);
  }

  // Test PlayerFish growth logic
  boolean testPlayerFishGrowth(Tester t) {
    return t.checkExpect(this.player.grow(10), this.playerIncrease)
        && t.checkExpect(this.player.grow(-1), this.playerDecrease)
        && t.checkExpect(this.player.grow(0), this.player);

  }

  // Test PlayerFish collision detection
  boolean testPlayerFishCollision(Tester t) {
    return t.checkExpect(this.player.checkCollision(nearFish), true)
        && t.checkExpect(this.player.checkCollision(farFish), false);
  }

  // tests the first compare radius method
  boolean testradiusCompare(Tester t) {
    return t.checkExpect(this.player.radiusCompare(10), true)
        && t.checkExpect(this.player.radiusCompare(30), false)
        && t.checkExpect(this.player.radiusCompare(20), false);
  }

  // test the second compare radius method
  boolean testradiusCompareTwo(Tester t) {
    return t.checkExpect(this.player.radiusCompareTwo(10), false)
        && t.checkExpect(this.player.radiusCompareTwo(30), true)
        && t.checkExpect(this.player.radiusCompareTwo(20), true);
  }

  // tests the addPlayerInScene method
  boolean addPlayerInScene(Tester t) {
    return t.checkExpect(this.player.addPlayerInScene(), this.playerInScene)
        && t.checkExpect(this.player2.addPlayerInScene(), this.player2InScene)
        && t.checkExpect(this.player3.addPlayerInScene(), this.player3InScene);
  }

  // Test background fish

  // Test BackgroundFish movement logic
  boolean testBackgroundFishMovement(Tester t) {
    BackgroundFish movedFish = bgFish.move();

    boolean withinBoundsX = movedFish.x >= 0 && movedFish.x <= 600;
    boolean withinBoundsY = movedFish.y >= 0 && movedFish.y <= 400;

    BackgroundFish edgeFish = new BackgroundFish(598, 398, Color.GREEN, 15);
    BackgroundFish edgeMovedFish = edgeFish.move();

    boolean outOfBoundsX = edgeMovedFish.x > 600;
    boolean outOfBoundsY = edgeMovedFish.y > 400;

    return t.checkExpect(withinBoundsX, true,
        "Background fish X position should wrap correctly within bounds")
        && t.checkExpect(withinBoundsY, true,
            "Background fish Y position should wrap correctly within bounds")
        && t.checkExpect(outOfBoundsX, false, "Edge case: X position should not exceed 600")
        && t.checkExpect(outOfBoundsY, false, "Edge case: Y position should not exceed 400");
  }

  // Test the radius comparison for eating logic in BackgroundFish
  boolean testBackgroundFishRadiusLarger(Tester t) {
    PlayerFish smallPlayer = new PlayerFish(300, 300, Color.BLUE, 10);
    PlayerFish largePlayer = new PlayerFish(300, 300, Color.BLUE, 20);

    return t.checkExpect(bgFish.checkRadiusLarger(smallPlayer), false)
        && t.checkExpect(bgFish.checkRadiusLarger(largePlayer), true);
  }

  // Test for losing condition when a bigger fish collides
  boolean testLoseConditionBackgroundFish(Tester t) {
    PlayerFish largePlayer = new PlayerFish(300, 300, Color.BLUE, 30);
    PlayerFish smallPlayer = new PlayerFish(300, 300, Color.BLUE, 10);

    return t.checkExpect(bgFish.lose(largePlayer), false)
        && t.checkExpect(bgFish.lose(smallPlayer), true);
  }

  // tests the calculate radius method
  boolean testCalculateRadius(Tester t) {
    PlayerFish largePlayer = new PlayerFish(300, 300, Color.BLUE, 30);
    BackgroundFish smallPlayer = new BackgroundFish(300, 300, Color.BLUE, 10);
    PlayerFish equalSizePlayer = new PlayerFish(300, 300, Color.BLUE, 20);
    BackgroundFish equalSizeBgFish = new BackgroundFish(300, 300, Color.RED, 20);
    PlayerFish minimalRadiusPlayer = new PlayerFish(300, 300, Color.BLUE, 1);
    BackgroundFish minimalRadiusBgFish = new BackgroundFish(300, 300, Color.RED, 1);

    return t.checkExpect(equalSizeBgFish.calculateRadius(equalSizePlayer), 40)
        && t.checkExpect(smallPlayer.calculateRadius(largePlayer), 40)
        && t.checkExpect(minimalRadiusBgFish.calculateRadius(minimalRadiusPlayer), 2);
  }

  // tests if the drawBackGroundFish is actually drawing the fish
  boolean testDrawBackgroundFish(Tester t) {
    BackgroundFish testFish = new BackgroundFish(150, 150, Color.GREEN, 20);
    WorldScene initialScene = new WorldScene(300, 300);
    WorldScene expectedScene = new WorldScene(300, 300).placeImageXY(testFish.makeFishBody(), 150,
        150);
    WorldScene actualScene = testFish.drawBackgroundFish(initialScene);
    return t.checkExpect(actualScene, expectedScene);
  }

  // ILoFish Tests

  // tests if the draw method is drawing
  boolean testDrawAll(Tester t) {
    WorldScene initialScene = new WorldScene(600, 400);
    WorldScene expectedScene = new WorldScene(600, 400);
    BackgroundFish fish1 = new BackgroundFish(100, 100, Color.RED, 10);
    BackgroundFish fish2 = new BackgroundFish(200, 200, Color.GREEN, 20);
    ILoFish fishList = new ConsLoFish(fish1, new ConsLoFish(fish2, new MtLoFish()));
    WorldImage fishImage1 = fish1.makeFishBody();
    WorldImage fishImage2 = fish2.makeFishBody();
    expectedScene = expectedScene.placeImageXY(fishImage1, 100, 100);
    expectedScene = expectedScene.placeImageXY(fishImage2, 200, 200);
    WorldScene resultScene = fishList.drawAll(initialScene);

    return t.checkExpect(resultScene, expectedScene);
  }

  // tests the countFish method
  boolean testCountFish(Tester t) {
    ILoFish multipleFishes = new ConsLoFish(new BackgroundFish(100, 100, Color.RED, 10),
        new ConsLoFish(new BackgroundFish(200, 200, Color.GREEN, 20),
            new ConsLoFish(new BackgroundFish(300, 300, Color.BLUE, 30), new MtLoFish())));
    ILoFish emptyFishList = new MtLoFish();

    return t.checkExpect(multipleFishes.countFishes(), 3)
        && t.checkExpect(emptyFishList.countFishes(), 0);
  }

  // tests the AddFish method
  boolean testAddFish(Tester t) {
    ILoFish initialList = new ConsLoFish(new BackgroundFish(100, 100, Color.RED, 15),
        new MtLoFish());
    ILoFish addedList1 = initialList.addFish(new BackgroundFish(300, 300, Color.BLUE, 25), 20, 1);
    ILoFish addedList2 = initialList.addFish(new BackgroundFish(300, 300, Color.BLUE, 25), 21, 1);
    ILoFish addedList3 = initialList.addFish(new BackgroundFish(300, 300, Color.BLUE, 25), 40, 10);
    ILoFish emptyList = new MtLoFish();
    ILoFish addedToEmpty = emptyList.addFish(new BackgroundFish(300, 300, Color.BLUE, 25), 20, 0);

    return t.checkExpect(addedList1.countFishes(), 2) && t.checkExpect(addedList2.countFishes(), 1)
        && t.checkExpect(addedList3.countFishes(), 1)
        && t.checkExpect(addedToEmpty.countFishes(), 1, "Should add fish to empty list");
  }

  // tests the checkAllCollisisons method
  boolean testCheckAllCollisions(Tester t) {
    ILoFish fishListClose = new ConsLoFish(new BackgroundFish(100, 100, Color.RED, 15),
        new MtLoFish());
    PlayerFish playerClose = new PlayerFish(100, 100, Color.BLUE, 30);
    ILoFish emptyList = new MtLoFish();
    PlayerFish anyPlayer = new PlayerFish(200, 200, Color.BLUE, 25);

    boolean collisionDetected = fishListClose.checkAllCollisions(playerClose);

    boolean noCollisionWithEmpty = emptyList.checkAllCollisions(anyPlayer);

    boolean noCollisionWithFar = fishListClose
        .checkAllCollisions(new PlayerFish(500, 500, Color.BLUE, 30));

    return t.checkExpect(collisionDetected, true, "Should detect collision when close")
        && t.checkExpect(noCollisionWithEmpty, false, "Should not detect collision with empty list")
        && t.checkExpect(noCollisionWithFar, false, "Should not detect collision when far");
  }

  // tests the RemoveEaten method
  boolean testRemoveEaten(Tester t) {
    ILoFish fishList = new ConsLoFish(new BackgroundFish(100, 100, Color.RED, 10),
        new ConsLoFish(new BackgroundFish(200, 200, Color.GREEN, 20), new MtLoFish()));
    PlayerFish player = new PlayerFish(100, 100, Color.BLUE, 15);
    ILoFish fishListNoEatable = new ConsLoFish(new BackgroundFish(100, 100, Color.RED, 20),
        new ConsLoFish(new BackgroundFish(200, 200, Color.GREEN, 25), new MtLoFish()));
    PlayerFish playerNoEatable = new PlayerFish(100, 100, Color.BLUE, 15);
    ILoFish emptyList = new MtLoFish();

    ILoFish newList = fishList.removeEaten(player);
    ILoFish newListNoEatable = fishListNoEatable.removeEaten(playerNoEatable);
    ILoFish newListEmpty = emptyList.removeEaten(player);

    return t.checkExpect(newList.countFishes(), 1)
        && t.checkExpect(newListNoEatable.countFishes(), 2)
        && t.checkExpect(newListEmpty.countFishes(), 0);
  }

  // tests to see cases in which the fish has won or not
  boolean testHasWon(Tester t) {
    ILoFish fishList = new ConsLoFish(new BackgroundFish(100, 100, Color.RED, 40),
        new ConsLoFish(new BackgroundFish(200, 200, Color.GREEN, 35), new MtLoFish()));
    PlayerFish player = new PlayerFish(200, 200, Color.BLUE, 30);
    ILoFish fishListOneSmaller = new ConsLoFish(new BackgroundFish(100, 100, Color.RED, 25),
        new ConsLoFish(new BackgroundFish(200, 200, Color.GREEN, 35), new MtLoFish()));

    ILoFish emptyList = new MtLoFish();

    return t.checkExpect(fishList.hasWon(player), false)
        && t.checkExpect(fishListOneSmaller.hasWon(player), false)
        && t.checkExpect(emptyList.hasWon(player), true);

  }

  // tests to see if the game knows when the player loses
  boolean testGetLose(Tester t) {
    ILoFish fishListLose = new ConsLoFish(new BackgroundFish(100, 100, Color.RED, 40),
        new MtLoFish());
    PlayerFish playerLose = new PlayerFish(100, 100, Color.BLUE, 10);
    ILoFish fishListNoLose = new ConsLoFish(new BackgroundFish(100, 100, Color.RED, 5),
        new MtLoFish());
    PlayerFish playerNoLose = new PlayerFish(100, 100, Color.BLUE, 15);
    ILoFish emptyList = new MtLoFish();
    PlayerFish playerEmpty = new PlayerFish(100, 100, Color.BLUE, 20);

    return t.checkExpect(fishListLose.getLose(playerLose), true)
        && t.checkExpect(fishListNoLose.getLose(playerNoLose), false)
        && t.checkExpect(emptyList.getLose(playerEmpty), false);

  }

  // GAME

  // tests to see if the game makes a scene (makeScene method)
  boolean testMakeScene(Tester t) {
    PlayerFish testPlayerFish = new PlayerFish(200, 200, Color.BLUE, 20);
    BackgroundFish testBackgroundFish1 = new BackgroundFish(100, 100, Color.RED, 10);
    BackgroundFish testBackgroundFish2 = new BackgroundFish(300, 300, Color.GREEN, 15);
    ILoFish testFishList = new ConsLoFish(testBackgroundFish1,
        new ConsLoFish(testBackgroundFish2, new MtLoFish()));
    Game testGame = new Game(testPlayerFish, testFishList, 0);
    WorldScene expectedScene = new WorldScene(600, 400)
        .placeImageXY(testPlayerFish.makeFishBody(), 200, 200)
        .placeImageXY(testBackgroundFish1.makeFishBody(), 100, 100)
        .placeImageXY(testBackgroundFish2.makeFishBody(), 300, 300);
    WorldScene actualScene = testGame.makeScene();

    return t.checkExpect(actualScene, expectedScene);
  }

  // tests to see if the game returns a message depending on if you win or lose
  // (lastScene test)
  boolean testLastScene(Tester t) {
    return t.checkExpect(this.game1.lastScene("You Won"), this.youWon)
        && t.checkExpect(this.game1.lastScene("You Lost"), this.youLost);
  }

  // tests to see if the game can see when a key is pressed (OnKeyEvent test)
  boolean testOnKeyEvent(Tester t) {
    Game initialGame = new Game(new PlayerFish(300, 300, Color.BLUE, 20), new MtLoFish(), 0);
    return t.checkExpect(initialGame.onKeyEvent("right"), this.playerMoveRightInGame)
        && t.checkExpect(initialGame.onKeyEvent("left"), this.playerMoveLeftInGame);
  }

}
