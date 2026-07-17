import React from 'react';
import { Link } from 'react-router-dom';
import { Helmet } from 'react-helmet-async';

export default function Landing() {
  return (
    <div className="flex flex-col space-y-20 py-10">
      
      {/* Dynamic SEO for this specific page */}
      <Helmet>
        <title>Revise | Master Your Exams</title>
        <meta 
          name="description" 
          content="Join Revise today. Use active recall and our 9-stage spacing cycle to permanently lock in your engineering and computer science concepts." 
        />
      </Helmet>

      {/* Hero Section */}
      <section className="text-center space-y-6">
        <h1 className="text-4xl sm:text-5xl md:text-6xl font-extrabold tracking-tight text-gray-900 dark:text-white">
          Don't just pass the exam.<br />
          <span className="text-blue-600 dark:text-blue-400">Remember it for life.</span>
        </h1>
        <p className="text-lg sm:text-xl text-gray-600 dark:text-gray-300 max-w-2xl mx-auto leading-relaxed">
          Revise is a spaced repetition platform designed to push information past your short-term memory. Lock in your knowledge today, tomorrow, and years from now.
        </p>
        <div className="pt-4">
          <Link
            to="/signup"
            className="inline-flex items-center justify-center px-8 py-3 text-base font-medium text-white bg-blue-600 rounded-lg hover:bg-blue-700 shadow-md transition-all duration-200 transform hover:scale-105"
          >
            Start Your First Cycle
          </Link>
        </div>
      </section>

      <hr className="border-gray-200 dark:border-gray-800" />

      {/* About Section */}
      <section className="space-y-6">
        <h2 className="text-3xl font-bold text-gray-900 dark:text-white">
          Why Revise?
        </h2>
        <p className="text-gray-600 dark:text-gray-300 text-lg leading-relaxed">
          Most students rely on the standard 1-3-7 day revision rule to cram for mid-semesters or final papers. While that works for getting a grade, the "forgetting curve" takes over immediately after. We built Revise to solve this. By dynamically stretching your review intervals from days to months to years, we ensure that the core concepts you work so hard to understand become a permanent part of your mental toolkit.
        </p>
      </section>

      {/* Active Recall Approach */}
      <section className="bg-gray-50 dark:bg-gray-800/50 rounded-2xl p-6 sm:p-8 space-y-6 border border-gray-100 dark:border-gray-800">
        <h2 className="text-3xl font-bold text-gray-900 dark:text-white">
          The Power of Active Recall
        </h2>
        <div className="space-y-4 text-gray-600 dark:text-gray-300 text-lg leading-relaxed">
          <p>
            Re-reading notes is a passive illusion of competence. Active recall is the process of actively stimulating your memory to retrieve a piece of information without looking at the source.
          </p>
          <p>
            Instead of simply re-reading your notes on database normal forms or algorithm time complexities, Revise prompts you to pull that information from scratch. The harder your brain works to retrieve the memory, the stronger the neural pathway becomes.
          </p>
        </div>
      </section>

      {/* The Lifelong Cycle Section */}
      <section className="space-y-8">
        <div className="space-y-4">
          <h2 className="text-3xl font-bold text-gray-900 dark:text-white">
            The Lifelong Revision Cycle
          </h2>
          <p className="text-gray-600 dark:text-gray-300 text-lg leading-relaxed">
            Our backbone is an exponentially increasing interval system. Every time you successfully recall a topic, the time until your next review expands.
          </p>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="border-b-2 border-gray-200 dark:border-gray-700">
                <th className="py-3 px-4 font-semibold text-gray-900 dark:text-white">Stage</th>
                <th className="py-3 px-4 font-semibold text-gray-900 dark:text-white">Interval</th>
                <th className="py-3 px-4 font-semibold text-gray-900 dark:text-white">Focus</th>
              </tr>
            </thead>
            <tbody className="text-gray-600 dark:text-gray-300">
              <tr className="border-b border-gray-100 dark:border-gray-800 transition-colors hover:bg-gray-50 dark:hover:bg-gray-800/50">
                <td className="py-3 px-4 font-medium">Stage 1 to 3</td>
                <td className="py-3 px-4">Days 1, 3, 7</td>
                <td className="py-3 px-4">Initial encoding & short-term consolidation</td>
              </tr>
              <tr className="border-b border-gray-100 dark:border-gray-800 transition-colors hover:bg-gray-50 dark:hover:bg-gray-800/50">
                <td className="py-3 px-4 font-medium">Stage 4 to 5</td>
                <td className="py-3 px-4">16 Days, 35 Days</td>
                <td className="py-3 px-4">Bridging into long-term memory storage</td>
              </tr>
              <tr className="border-b border-gray-100 dark:border-gray-800 transition-colors hover:bg-gray-50 dark:hover:bg-gray-800/50">
                <td className="py-3 px-4 font-medium">Stage 6 to 7</td>
                <td className="py-3 px-4">4 Months, 6 Months</td>
                <td className="py-3 px-4">Deep retention & permanent integration</td>
              </tr>
              <tr className="transition-colors hover:bg-gray-50 dark:hover:bg-gray-800/50">
                <td className="py-3 px-4 font-medium">Stage 8+</td>
                <td className="py-3 px-4">1 to 2+ Years</td>
                <td className="py-3 px-4">Lifelong maintenance check</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      {/* Footer */}
      <footer className="pt-10 border-t border-gray-200 dark:border-gray-800 text-center text-gray-500 dark:text-gray-400 text-sm">
        <p>&copy; {new Date().getFullYear()} Revise. All rights reserved.</p>
        <p>Developed & Design by <Link to={'https://github.com/TagCoding223'}>Vishal Chouhan</Link>.</p>
        <div className="mt-4 flex justify-center space-x-6">
          <Link to="/legal#about" className="hover:text-gray-900 dark:hover:text-white transition-colors">
            About Us
          </Link>
          <Link to="/legal#privacy-policy" className="hover:text-gray-900 dark:hover:text-white transition-colors">
            Privacy Policy
          </Link>
          <Link to="/legal#terms-and-service" className="hover:text-gray-900 dark:hover:text-white transition-colors">
            Terms of Service
          </Link>
        </div>
      </footer>
      
    </div>
  );
}