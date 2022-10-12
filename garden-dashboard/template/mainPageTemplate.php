  <?php if(isSet($_GET["formmsg"])): ?>
  <div class="error">
    <h5 class="text-center">Attenzione!</h5>
    <p class="text-center"><?php echo $_GET["formmsg"]; ?></p>
  </div>
  <?php endif; ?>
  <div class="container">
    <div class="row">
      <h3>Garden Status</h3>
      <div class="col-sm-6">
        <p id="conn"></p>
        <p>Garden Lamp 1:</p>
        <p>Garden Lamp 2:</p>
        <p>Garden Lamp 3:</p>
        <p>Garden Lamp 4:</p>
        <p>Irrigation Status:</p>
        <p>Temperature:</p>
        <p>Brightness:</p>
        <p>Garden Mode</p>
      </div>
      <div class="col-sm-6">
        <p id="conn2"></p>
        <p id="LM1">__</p>
        <p id="LM2">__</p>
        <p id="LM3">__</p>
        <p id="LM4">__</p>
        <p id="IRR">__</p>
        <p id="temp">__</p>
        <p id="light">__</p>
        <p id="MODE">AUTO</p>
      </div>
    </div>
  </div>
