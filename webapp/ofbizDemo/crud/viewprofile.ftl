
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">

    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Of Biz Party Manager</title>
</head>
<body>
<form  method="post" action= "veiwprofile.groovy" >
    <div>
        <h2>Create Customer</h2>

        <label for="pId">Party ID</label>
        <input type="text" name="party_id" id="pId">

        <label for="title">Title</label>
        <input type="text" name="Title" id="title">

        <label for="fName">First Name</label>
        <input type="text" name="first_name" id="fName" required>

        <label for="mName">Middle initial</label>
        <input type="text" name="middle_name" id="mName">

        <label for="lName">Last Name</label>
        <input type="text" name="last_name" id="lName" required>

        <label for="suffix">Suffix</label>
        <input type="text" name="Suffix" id="suffix">

    </div>

    <div>
        <h2>Mailing/Shipping Address</h2>

        <label for="add1">Address 1</label>
        <input type="text" name="address_1" id="add1" required>

        <label for="add2">Address 2</label>
        <input type="text" name="address_2" id="add2">

        <label for="city">City</label>
        <input type="text" name="city" id="city" required>

        <label for="state">State</label>
        <input type="text" name="state" id="state">

        <label for="pCode">Zip/Postal Code</label>
        <input type="number" name="postal_code" id="pCode" required>

        <label for="country">Country</label>
        <input type="text" name="country" id="country">

        <label for="aas">Allow Address Solicitation</label>
        <select id="aas" name="allow_address_solicitation">
            <option value="yes">Yes</option>
            <option value="no">NO</option>
        </select>
    </div>

    <div>
        At least ome phone number is requires below.

        <h2>Home Phone</h2>>

        <label for="hcCode">Country Code</label>
        <input type="text" name="hcountry_code" id="hcCode">

        <label for="haCode">Area Code</label>
        <input type="number" name="harea_code" id="haCode">

        <label for="hpNumber">Phone Number</label>
        <input type="number" name="hphone_number" id="hpNumber">

        <label for="hext">ext</label>
        <input type="text" name="hext" id="hext">

        <label for="haSolicitation">Allow Solicitation</label>
        <select id="haSolicitation" name="hallow_solicitation">
            <option value="yes">Yes</option>
            <option value="no">NO</option>
        </select>
    </div>

    <div>
        <h2>Work Phone Number</h2>

        <label for="wcCode">Country Code</label>
        <input type="text" name="wcountry_code" id="wcCode">

        <label for="waCode">Area Code</label>
        <input type="number" name="warea_code" id="waCode">

        <label for="wpNumber">Phone Number</label>
        <input type="number" name="wphone_number" id="wpNumber">

        <label for="wext">ext</label>
        <input type="text" name="wext" id="wext">

        <label for="waSolicitation">Allow Solicitation</label>
        <select id="waSolicitation" name="wallow_solicitation">
            <option value="yes">Yes</option>
            <option value="no">NO</option>
        </select>
    </div>

    <div>
        <h2>Fax Number</h2>

        <label for="fcCode">Country Code</label>
        <input type="text" name="fcountry_code" id="fcCode">

        <label for="faCode">Area Code</label>
        <input type="number" name="farea_code" id="faCode">

        <label for="fpNumber">Phone Number</label>
        <input type="number" name="fphone_number" id="fpNumber">

        <label for="fext">ext</label>
        <input type="text" name="fext" id="fext">

        <label for="faSolicitation">Allow Solicitation</label>
        <select id="faSolicitation" name="fallow_solicitation">
            <option value="yes">Yes</option>
            <option value="no">NO</option>
        </select>
    </div>

    <div>
        <h2>Mobile Phone Number</h2>

        <label for="mcCode">Country Code</label>
        <input type="text" name="mcountry_code" id="mcCode">

        <label for="maCode">Area Code</label>
        <input type="number" name="marea_code" id="maCode">

        <label for="mpNumber">Phone Number</label>
        <input type="number" name="mphone_number" id="mpNumber">

        <label for="maSolicitation">Allow Solicitation</label>
        <select id="maSolicitation" name="mallow_solicitation">
            <option value="yes">Yes</option>
            <option value="no">NO</option>
        </select>
    </div>

    <div>
        <h2>E-Mail Address</h2>

        <label for="email">Email</label>
        <input type="email" name="e_mail" id="email" required>

        <label for="eaSolicitation">Allow Solicitation</label>
        <select id="eaSolicitation" name="eallow_solicitation">
            <option value="yes">Yes</option>
            <option value="no">NO</option>
        </select>

        <label for="uName">User Name</label>
        <input type="text" name="user_name" id="uName">

        <label for="pWord">Password</label>
        <input type="password" name="password" id="pWord" required>

        <label for="cpWord">Password</label>
        <input type="password" name="confirm_password" id="cpWord" required>

        <label for="secQuestion">Security Question</label>
        <select id="secQuestion" name="security_question">
            <option value="question1">Question 1</option>
            <option value="question2">Question 2</option>
        </select>

        <label for="secAnswer">Security Answer</label>
        <input type="text" name="security_answer" id="secAnswer">




    </div>
    <input type="submit" value="Save">
<a href="veiw_profile.ftl">save</a>
</form>
</body>
</html>
