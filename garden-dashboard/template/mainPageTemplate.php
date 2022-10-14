  <?php if(isSet($_GET["formmsg"])): ?>
  <div class="error">
    <h5 class="text-center">Attenzione!</h5>
    <p class="text-center"><?php echo $_GET["formmsg"]; ?></p>
  </div>
  <?php endif; ?>
  <div class="container">
    <h3>Garden Status</h3>
    <table>
      <tr>
        <th><p id="conn"></p></th>
        <th><p id="conn2"></p></th>
      </tr>
      <tr>
        <td><p>Garden Lamp 1:</p></td>
        <td><p id="LM1">__</p></td>
      </tr>
      <tr>
        <td><p>Garden Lamp 2:</p></td>
        <td><p id="LM2">__</p></td>
      </tr>
      <tr>
        <td><p>Garden Lamp 3:</p></td>
        <td><p id="LM3">__</p></td>
      </tr>
      <tr>
        <td><p>Garden Lamp 4:</p></td>
        <td><p id="LM4">__</p></td>
      </tr>
      <tr>
        <td><p>Irrigation Status:</p></td>
        <td><p id="IRR">__</p></td>
      </tr>
      <tr>
        <td><p>Temperature:</p></td>
        <td><p id="temp">__</p></td>
      </tr>
      <tr>
        <td><p>Brightness:</p></td>
        <td><p id="light">__</p></td>
      </tr>
      <tr>
        <td><p>Garden Mode:</p></td>
        <td><p id="MODE">AUTO</p></td>
      </tr>
    </table>
  </div>
