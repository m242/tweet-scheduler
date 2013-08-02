delay = (ms, func) -> setTimeout func, ms

hideArray = []

loadScheduled = ->
  future = $.getJSON "/api/scheduled?_=" + new Date().getTime()
  future.done (data) ->
    $.each hideArray, (i, timeout) -> clearTimeout(timeout)
    if data.length > 0
      now = new Date().getTime()
      $tweetList = $("#tweetList")
      html = "<h3 class=\"hdr\"><em class=\"icon-twitter\"></em>My Scheduled Tweets</h3>"
      $.each data, (i, tweet) ->
        hideArray.push(delay(tweet.millis, -> hideTweet(tweet._id)))
        html += "<div class=\"tweet\" id=\"" + tweet._id + "\"><div class=\"pull-right\"><em class=\"icon-remove\" title=\"Remove\"></em></div><div class=\"time\">" +
          moment(now + tweet.millis).format("MM/DD/YYYY hh:mm A") +
          "</div><div class=\"body\">" + tweet.tweet + "</div></div>"
      $tweetList.html html
  future.fail () ->
    return

calculateMillis = ->
  sch = moment($("#datepicker").val() + " " + $("#timepicker").val(), "MM/DD/YYYY hh:mm A")
  if sch.isValid() then sch.valueOf() - new Date().getTime() else 0

displayMillis = (millis) ->
  if millis > 0
    $("#postTime").html("This tweet will be posted <strong>" + moment.duration(millis, "milliseconds").humanize(true) + "</strong>.").removeClass("warning")
  else
    errorMillis()

errorMillis = ->
  $("#postTime").html("Doc, it's Marty - we can't go back in time!").addClass("warning")

errorTweet = ->
  $("#postTime").html("Whoops! Make sure you have something to say.").addClass("warning")
  $("#tweet").addClass("error")

hideTweet = (id) ->
  $("#" + id).fadeOut("slow", ->
    $("#" + id).remove()
    if $("#tweetList").find(".tweet").length is 0 then $("#tweetList .hdr").remove()
  )

$ ->

  $("html").removeClass "no-js"

  $("#loggedIn").each ->

    loadScheduled()

    $tweetForm = $("#tweetForm")
    $submit = $tweetForm.find(".btn-block")

    oneHour = clientTime + (1000 * 60 * 60)
    oneHourMoment = moment(oneHour)

    $("#datepicker").datepicker({
      format: "mm/dd/yyyy"
    }).datepicker("setValue", new Date(oneHour)).on "changeDate", ->
      displayMillis(calculateMillis())

    $("#timepicker").timepicker({
      minuteStep: 1
      showInputs: false
      disableFocus: false
    }).val(oneHourMoment.format("hh:mm A"))

    $characters = $("#characters")

    $tweetForm.on "input", "#tweet", ->
      chars = $("#tweet").val().length
      left = 140 - chars
      $characters.html(left)
      if left < 0 then $characters.addClass "warning" else $characters.removeClass "warning"

    $tweetForm.on "change", ".input", ->
      displayMillis(calculateMillis())

    $tweetForm.on "submit", ->
      $("#tweet").removeClass("error")
      millis = calculateMillis()
      displayMillis(millis)
      if $("#tweet").val().length == 0
        errorTweet()
      else if millis <= 0
        errorMillis()
      else
        $submit.addClass("disabled").find(".icon").addClass("icon-cog").removeClass("icon-ok-circle")
        future = $.post "/api/tweet", { "tweet": $("#tweetForm textarea").val(), "millis": millis }
        future.done () ->
          $submit.hide()
          $("#postSuccess").fadeIn(400).delay(4000).fadeOut(400, -> $submit.show());
          $("#tweet").val("")
          $characters.html("140")
          loadScheduled()
          _gaq.push(['_trackEvent', 'Tweets', 'Schedule', $("#screenName").html()])
        future.fail (data) ->
          err = JSON.parse(data.responseText)
          if err.tweet?
            errorTweet()
          if err.millis?
            errorMillis()
        future.always ->
          $submit.removeClass("disabled").find(".icon").addClass("icon-ok-circle").removeClass("icon-cog")
      false

    $tweetList = $("#tweetList")

    $tweetList.on "click", ".icon-remove", ->
      $(this).parent().html("<button class=\"button btn btn-info btn-small btn-remove\">Remove</button>")

    $tweetList.on "click", ".btn-remove", ->
      $this = $(this)
      id = $this.parent().parent().attr("id")
      hideTweet(id)
      $.ajax({ type: "DELETE", url: "/api/tweet/" + encodeURIComponent(id) })
      _gaq.push(['_trackEvent', 'Tweets', 'Remove', $("#screenName").html()])



  $("#signout").each ->

    $signin = $("#signin")
    $signin.find(".icon").removeClass("fadeinout-enabled")
    $signin.on "click", -> $signin.find(".icon").addClass("fadeinout-enabled")

  $("#index").each ->

    $signin = $("#signin")
    $signin.find(".icon").removeClass("fadeinout-enabled")
    $signin.on "click", -> $signin.find(".icon").addClass("fadeinout-enabled")

