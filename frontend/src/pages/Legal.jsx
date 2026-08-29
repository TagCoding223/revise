import React, { useEffect } from 'react';
import { useLocation, Link } from 'react-router-dom';
import { Helmet } from 'react-helmet-async';

export default function Legal() {
  const { hash } = useLocation();

  // Handle smooth scrolling to the specific section based on the URL hash
  useEffect(() => {
    if (hash) {
      const targetId = hash.replace('#', '');
      const element = document.getElementById(targetId);
      if (element) {
        // A slight timeout ensures the page is fully rendered before scrolling
        setTimeout(() => {
          element.scrollIntoView({ behavior: 'smooth', block: 'start' });
        }, 100);
      }
    } else {
      window.scrollTo(0, 0);
    }
  }, [hash]);

  return (
    <div className="max-w-4xl mx-auto px-4 py-12 sm:px-6 lg:px-8 animate-in fade-in duration-300">
      <Helmet>
        <title>Legal, Privacy & Terms | Revise</title>
        <meta name="description" content="Read the Revise Terms of Service, Privacy Policy, and learn more about our mission to help you retain information permanently." />
      </Helmet>
      {/* Page Header */}
      <div className="text-center mb-16">
        <h1 className="text-4xl font-extrabold text-gray-900 dark:text-white tracking-tight mb-4">
          Legal & Information
        </h1>
        <p className="text-lg text-gray-600 dark:text-gray-400">
          Everything you need to know about our mission, your privacy, and our terms of service.
        </p>

        {/* Quick Jump Links */}
        <div className="mt-8 flex flex-wrap justify-center gap-4">
          <Link to="#about" className="px-4 py-2 bg-gray-100 dark:bg-gray-800 text-gray-700 dark:text-gray-300 rounded-full text-sm font-medium hover:bg-blue-50 hover:text-blue-600 dark:hover:bg-gray-700 dark:hover:text-blue-400 transition-colors cursor-pointer">
            About Revise
          </Link>
          <Link to="#privacy" className="px-4 py-2 bg-gray-100 dark:bg-gray-800 text-gray-700 dark:text-gray-300 rounded-full text-sm font-medium hover:bg-blue-50 hover:text-blue-600 dark:hover:bg-gray-700 dark:hover:text-blue-400 transition-colors cursor-pointer">
            Privacy Policy
          </Link>
          <Link to="#terms" className="px-4 py-2 bg-gray-100 dark:bg-gray-800 text-gray-700 dark:text-gray-300 rounded-full text-sm font-medium hover:bg-blue-50 hover:text-blue-600 dark:hover:bg-gray-700 dark:hover:text-blue-400 transition-colors cursor-pointer">
            Terms & Conditions
          </Link>
        </div>
      </div>

      <div className="space-y-20">

        {/* --- ABOUT SECTION --- */}
        <section id="about" className="scroll-mt-24">
          <div className="border-b border-gray-200 dark:border-gray-800 pb-4 mb-8">
            <h2 className="text-3xl font-bold text-gray-900 dark:text-white flex items-center">
              <svg className="w-8 h-8 mr-3 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              About Revise
            </h2>
          </div>
          <div className="prose prose-blue dark:prose-invert max-w-none text-gray-600 dark:text-gray-300 space-y-6">
            <p>
              Welcome to <strong>Revise</strong>, your intelligent study companion. We built this platform with a single mission: to help students and lifelong learners beat the "forgetting curve" and retain information permanently.
            </p>
            <p>
              Traditional studying often involves cramming, which leads to rapid memory decay. Revise utilizes a scientifically proven <strong>Spaced Repetition Algorithm</strong>. By analyzing how often you review a topic, our system dynamically schedules your next revision date—increasing the interval as your mastery of the subject grows.
            </p>
            <p>
              Whether you are preparing for a university exam, learning a new programming language, or mastering database management systems, Revise ensures you review the right material at exactly the right time. Work smarter, not harder.
            </p>
          </div>
        </section>

        {/* --- PRIVACY POLICY SECTION --- */}
        <section id="privacy" className="scroll-mt-24">
          <div className="border-b border-gray-200 dark:border-gray-800 pb-4 mb-8">
            <h2 className="text-3xl font-bold text-gray-900 dark:text-white flex items-center">
              <svg className="w-8 h-8 mr-3 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
              </svg>
              Privacy Policy
            </h2>
          </div>
          <div className="prose prose-blue dark:prose-invert max-w-none text-gray-600 dark:text-gray-300 space-y-6">
            <p className="text-sm font-medium">Last Updated: July 2026</p>
            <p>
              Your privacy is critically important to us. At Revise, we have a few fundamental principles: we don't ask you for personal information unless we truly need it, and we don't share your personal information with anyone except to comply with the law, develop our products, or protect our rights.
            </p>
            <h3 className="text-xl font-semibold text-gray-900 dark:text-white mt-8 mb-4">1. Data We Collect</h3>
            <ul className="list-disc pl-6 space-y-2">
              <li><strong>Account Information:</strong> When you sign up via Email or Google Auth, we collect your name, email address, and authentication tokens.</li>
              <li><strong>Study Data:</strong> The topics you create, links you save, and the timestamps of your revisions are stored to power the spaced repetition algorithm.</li>
            </ul>

            <h3 className="text-xl font-semibold text-gray-900 dark:text-white mt-8 mb-4">2. How We Use Your Data</h3>
            <p>
              We use the collected information exclusively to provide and maintain the Service. Specifically, your study data is used to calculate and schedule your daily revision dashboard. We <strong>do not</strong> sell your data to third-party advertisers.
            </p>

            <h3 className="text-xl font-semibold text-gray-900 dark:text-white mt-8 mb-4">3. Data Security</h3>
            <p>
              We implement industry-standard security measures, including JSON Web Tokens (JWT) for session management and BCrypt password hashing, to protect against unauthorized access or data breaches.
            </p>
          </div>
        </section>

        {/* --- TERMS AND CONDITIONS SECTION --- */}
        <section id="terms" className="scroll-mt-24">
          <div className="border-b border-gray-200 dark:border-gray-800 pb-4 mb-8">
            <h2 className="text-3xl font-bold text-gray-900 dark:text-white flex items-center">
              <svg className="w-8 h-8 mr-3 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
              </svg>
              Terms & Conditions
            </h2>
          </div>
          <div className="prose prose-blue dark:prose-invert max-w-none text-gray-600 dark:text-gray-300 space-y-6">
            <p>
              By accessing and using Revise, you agree to comply with and be bound by the following terms and conditions. If you disagree with any part of these terms, please do not use our service.
            </p>

            <h3 className="text-xl font-semibold text-gray-900 dark:text-white mt-8 mb-4">1. Use of Service</h3>
            <p>
              Revise is provided for your personal, non-commercial educational use. You agree to use the platform only for lawful purposes and in a way that does not infringe the rights of, restrict, or inhibit anyone else's use and enjoyment of the platform.
            </p>

            <h3 className="text-xl font-semibold text-gray-900 dark:text-white mt-8 mb-4">2. Account Responsibilities</h3>
            <p>
              You are responsible for maintaining the confidentiality of your account credentials. You agree to accept responsibility for all activities that occur under your account. If you believe your account has been compromised, you must reset your password immediately.
            </p>

            <h3 className="text-xl font-semibold text-gray-900 dark:text-white mt-8 mb-4">3. Limitation of Liability</h3>
            <p>
              The materials on Revise are provided on an 'as is' basis. We make no warranties, expressed or implied, regarding the accuracy, reliability, or availability of the study scheduling algorithm. We shall not be liable for any data loss, exam failures, or interruptions to the service.
            </p>

            <h3 className="text-xl font-semibold text-gray-900 dark:text-white mt-8 mb-4">4. Account Termination</h3>
            <p>
              We reserve the right to suspend or terminate your account at our sole discretion, without prior notice, if you breach any of these Terms.
            </p>
          </div>
        </section>

      </div>
    </div>
  );
}