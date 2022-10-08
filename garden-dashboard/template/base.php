<!DOCTYPE html>

<html lang="en">
  <head>
        <!-- Required meta tags -->
        <meta charset="utf-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <!-- Bootstrap CSS -->
        <?php if(isset($templateParams["css"])): ?>
          <?php for($i = 0; $i < count($templateParams["css"]); $i++): ?>
            <link href="<?php echo $templateParams["css"][$i] ?>" rel="stylesheet" type="text/css">
          <?php endfor; ?>
        <?php endif; ?>

        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-1BmE4kWBq78iYhFldvKuhfTAU6auU8tT94WrHftjDbrCEXSU1oBoqyl2QvZ6jIW3" crossorigin="anonymous">
        <title>Garden Dashboard</title>
        <link rel="icon" type="image/x-icon" href="favicon/favicon.ico">
  </head>
    <body>
      <!-- Optional JavaScript -->
      <!-- jQuery first, then Popper.js, then Bootstrap JS -->
      <script src="jsUtils/jquery-1.11.3.min.js" type="text/javascript"></script>

      <main>
        <header id="myHeader">
          <nav class="navbar navbar-collapse-lg navbar-light bg-light">
            <div class="container-fluid">
              <h1>Garden Dashboard</h1>
              <img src="images/garden.webp" id="logoImg">             
            </div>
          </nav>
        </header>

        <!-- qua va il codice dinamico -->
        <?php
          if(isset($templateParams["pagereq"])){
            require($templateParams["pagereq"]);
          }
        ?>
      </main>
    </body>
    <script src="https://cdn.jsdelivr.net/npm/@popperjs/core@2.10.2/dist/umd/popper.min.js" integrity="sha384-7+zCNj/IqJ95wo16oMtfsKbZ9ccEh31eOz1HGyDuCQ6wgnyJNSYdrPa03rtR1zdB" crossorigin="anonymous"></script>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.min.js" integrity="sha384-QJHtvGhmr9XOIpI6YVutG+2QOK9T+ZnN4kzFN1RtK3zEFEIsxhlmWl5/YESvpZ13" crossorigin="anonymous"></script>
    <script type="text/javascript "src="javascript/ajaxutils.js"></script>
    <script type="text/javascript "src="javascript/mainPage.js"></script>
    <script type="text/javascript "src="javascript/getDynamicGarden.js"></script>
</html> 
