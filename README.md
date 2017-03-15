## tako 

🐙 tako is a Github notifier for Slack. It is written in Clojure and can be deployed to AWS  lambda.

![blame imgur if you see this](http://i.imgur.com/mamKBe7.png)

## Why?

These are some (personal?) reasons to use tako:

- **Email kinda sucks**. By default Github sends you an email everytime someone creates a pull request or issue on a project you follow, or when you are mentioned, etc. Being an *inbox zero* freak myself, depending on email to stay on top of what's going on my your github account is rather cumbersome.
- **Slack does a decent job** You probably use it more than email anyway. Slack messages are easier to skim and keep clean. Most notifications don't require you to reply back.


## Requirements

- [lein](http://leiningen.org)
- A [Github token](https://github.com/settings/tokens)
- A [Slack Webhook URL](https://slack.com/apps/new/A0F7XDUAZ-incoming-webhooks) pointing to a channel of your preference.
- [Enable web notifications](https://github.com/settings/notifications) on Github

To run locally:

    lein run YOUR_GITHUB_TOKEN YOUR_SLACK_WEBHOOK_URL

You can also create an jar with `lein uberjar`.

## Running from AWS lambda

In my current setup I have `tako` running on top of [AWS lambda](https://aws.amazon.com/lambda/). This is why it is cool:

- [It is free](https://aws.amazon.com/lambda/pricing/), at least for this use case. Since `tako` runs just a few times per hour, it will never be executed more than one million times a month (which is when Amazon starts charging you).
- You don't need to care about configure/maintain a server
- It is easy to update/deploy the code

All you have to do is create a new AWS lambda function and configure it:

0: Build a jar
```bash
git clone git@github.com:casidiablo/tako.git
cd tako
export GITHUB_TOKEN=your token
export SLACK_WEBHOOK_URL=the slack webhook url
./build-jar

# a jar will be assembled in tako/target/tako.jar
# by the way, the credentials are baked into the jar
```
1. Sign in to the AWS Management Console and open the AWS Lambda console at https://console.aws.amazon.com/lambda/
2. Choose Create a Lambda function.
3. In **Step 1: Select blueprint**, choose the **sns-message** blueprint.
4. In **Step 2: Configure event source**:
  - In **Event source type**, choose **SNS**.
  - In **SNS topic** type `arn:aws:sns:us-east-1:821676841978:clock`. This is a public SNS topic that sends a message every minute, perpetually, amen.
5. In **Step 3**: Configure function, do the following:
  - Set the function name to `tako`
  - In **Runtime**, specify *Java 8*
  - In **Handler** type `tako.core::handler`
  - In **Role**, choose basic execution role.
  - In the **Lambda function code**, choose the jar produced in step `0`
  - In **Memory (MB)**, set the dropdown to 256
  - In **Timeout**, configure it to be 30 seconds
6. In **Step 4: Review**, review the configuration and then choose **Create Function**. The function must look [like this](http://i.imgur.com/3tduRGF.png).

### Updating code

You can upload changes from the command line by running:

    # make sure you have awscli installed and configured
    ./lambda-deploy

Or by building a new jar and uploading it manually through the AWS web console.

## Why not to use this

There's many reasons not to use this:

- **Can't reply notifications directly from Slack**. Email is way better here. If you rely a lot on this feature, this is not for you.
- Slack has serious **push notification** problems on Android. It is not 100% reliable. If you need to attend every notification timely it defintely could be an issue. That said, I hardly use my cellphone for this kind of stuff anyway. I built this with Slack Desktop in mind.
- If you hate any of these things: Jeff Bezos, JVM, clojure, AWS, Slack... then it is probably not for you.


### Official Github/Slack integration

Github provides an official [Slack integration](https://github.com/integrations/slack) and is super cool and useful when you want to focus on specific repositories and you want details of every commit pushed to the repo (you have to manually choose which repos you want to get slack messages from). I highly recommend this over `tako` for such cases. It has some quircks like notifying you of code you just pushed, which is stupid, but whatever it works well and it's easier to configure than this project. That said, I built tako to replace the notifications inbox of the Github web and avoid email; and it works for _all_ the repos you watch, not only a few ones that you cherry pick.
